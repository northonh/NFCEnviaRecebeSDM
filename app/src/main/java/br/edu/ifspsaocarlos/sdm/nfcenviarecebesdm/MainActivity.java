package br.edu.ifspsaocarlos.sdm.nfcenviarecebesdm;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    // Referência para elementos de UI
    private EditText mensagemEnviarET;
    private TextView mensagemRecebidaTV;
    private Button preparaEnvioButton;

    // Constantes para salvar/restaurar tela principal
    private final String MENSAGEM_ENVIAR = "MENSAGEM_ENVIAR";
    private final String MENSAGEM_RECEBIDA = "MENSAGEM_RECEBIDA";
    private final String ESTADO_UI = "ESTADO_UI";

    // Referência para o Adaptador padrão NFC
    private NfcAdapter adaptadorNFC;

    // RequestCode para o Contato Picker
    private int SELECIONA_CONTATO_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Recuperando referências para os elementos de UI
        mensagemEnviarET = findViewById(R.id.et_mensagem_enviar);
        mensagemRecebidaTV = findViewById(R.id.tv_mensagem_recebida);
        preparaEnvioButton = findViewById(R.id.bt_prepara_envio);

        // Checando se o Adaptador NFC existe
        adaptadorNFC = NfcAdapter.getDefaultAdapter(this);
        if (adaptadorNFC == null) {
            Toast.makeText(this, R.string.adaptador_nfc_necessario, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /* Trata o clique no botão, travando os elementos de UI até que a mensagem seja enviada */
    public void preparaEnvio(View view) {
        if (view.getId() == R.id.bt_prepara_envio) {
            // Prepara o envio da mensagem
            criaMensagemNdef();

            // Trava os elementos de UI
            ativaUI(false);
            Toast.makeText(this, R.string.mensagem_preparada, Toast.LENGTH_SHORT).show();
        }
    }

    private void ativaUI(boolean estado) {
        mensagemEnviarET.setEnabled(estado);
        preparaEnvioButton.setEnabled(estado);
    }

    private void criaMensagemNdef() {
        /* 2 porque o primeiro vai ser o registro do aplicativo e o segundo a mensagem de fato */
        NdefRecord[] registrosNdef = new NdefRecord[2];

        /* O registro do aplicativo pode estar em qualquer posição do vetor e indica ao receptor
        *  qual aplicativo queremos que receba o  ACTION_NDEF_DISCOVERED. Podem existir vários
        *  registros de aplicativo. Se nenhum for encontrado, o Google Play é aberto para a
        *  instalação*/
        registrosNdef[0] = NdefRecord.createApplicationRecord(getPackageName());

        String mensagem = mensagemEnviarET.getText().toString();
        NdefRecord registroNdefMensagem = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], mensagem.getBytes());
        registrosNdef[1] = registroNdefMensagem;

        /* Criando a mensagem NDEF com os registros acima */
        NdefMessage mensagemNdef = new NdefMessage(registroNdefMensagem);

        /* Passando a mensagem para o Adaptador para que seja enviada quando um dispositivo NFC
        * for encontrado. Também é possível criar a mensagem somente quando um
        * ACTION_NDEF_DISCOVERED for lançado. Para isso é necessário implementar as Interfaces
        * NfcAdapter.CreateNdefMessageCallback (método createNdefMessage) e
        * NfcAdapter.OnNdefPushCompleteCallback (método onNdefPushComplete) */
        adaptadorNFC.setNdefPushMessage(mensagemNdef, this);
    }

    /* Chamado pelo SO quando ACTION_NDEF_DISCOVERED é lançada */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Vai tratar o recebimendo e visualização da mensagem
        recebeMensagemNdef(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        recebeMensagemNdef(getIntent());
    }

    private void recebeMensagemNdef(Intent intent) {
        /* Se uma mensagem NDEF foi identificada, receber a mensagem */
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            /* Extrai um vetor de Mensagem NDEF dos parâmetros da Intent */
            Parcelable[] mensagensNdefRecebidas =  intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            /* Se o vetor não está vazio */
            if(mensagensNdefRecebidas != null) {
                /* Parseia a Mensagem NDEF da primeira posição que, pela nossa implementação, é
                * única */
                NdefMessage mensagemNdefRecebida = (NdefMessage) mensagensNdefRecebidas[0];

                /* Extrai um vetor de Registros NDEF da Mensagem NDEF */
                NdefRecord[] registroNdefMensagemRececida = mensagemNdefRecebida.getRecords();

                /* O vetor de registros deve conter 2 registros, o primeiro que é o registro do
                *  aplicativo e o segundo que é o registro que contém o payload (mensagem de texto)*/
                for (NdefRecord record : registroNdefMensagemRececida) {
                    String mensagemTexto = new String(record.getPayload());

                    if (mensagemTexto.equals(getPackageName())) {
                        /* Se for o registro de aplicativo, mostra uma mensagem com o aplicativo
                        * remetente */
                        Toast.makeText(this, getString(R.string.recebida_de) + mensagemTexto, Toast.LENGTH_LONG).show();
                        continue;
                    }
                    else {
                        /* Se for o registro da mensagem de texto, mostra a mensagem no TextView*/
                        mensagemRecebidaTV.setText(mensagemTexto);
                        /* Ativa a UI para que uma nova mensagem de texto possa ser digitada */
                        ativaUI(true);
	                    /* Limpa o Edit Text */
                        mensagemEnviarET.setText("");
                    }
                }
            }
            else {
                /* Se nenhuma mensagem de texto foi recebida */
                Toast.makeText(this, R.string.nenhuma_mensagem_recebida, Toast.LENGTH_LONG).show();
            }
        }
    }

    /* Salva o texto da mensagem a enviar e o estado da UI */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Apesar da implementação padrão salvar os EditTexts automaticamente, forçaremos
        outState.putString(MENSAGEM_ENVIAR, mensagemEnviarET.getText().toString());
        outState.putString(MENSAGEM_RECEBIDA, mensagemRecebidaTV.getText().toString());

        // Como o estado da ativação do Botão e do EditText estão ligados, basta passar um deles
        outState.putBoolean(ESTADO_UI, mensagemEnviarET.isEnabled());
    }

    /* Restaura o texto da mensagem a enviar e o estado da UI */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            String mensagemEnviar = savedInstanceState.getString(MENSAGEM_ENVIAR);
            if (mensagemEnviar != null) {
                mensagemEnviarET.setText(mensagemEnviar);
            }
            String mensagemRecebida = savedInstanceState.getString(MENSAGEM_RECEBIDA);
            if (mensagemRecebida != null) {
                mensagemRecebidaTV.setText(mensagemRecebida);
            }
            boolean estadoUI = savedInstanceState.getBoolean(ESTADO_UI);
            ativaUI(estadoUI);
        }
    }

    public void selecionaContato(View view){
        if (view.getId() == R.id.buttonSelecionaContato) {
            Intent selecionaContatoIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(selecionaContatoIntent, SELECIONA_CONTATO_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECIONA_CONTATO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Recupera a URI do contato da resposta
                Uri contatoUri = data.getData();
                // Busca um Cursor para o contato
                Cursor cursor = getContentResolver().query(contatoUri, null, null, null, null);
                cursor.moveToFirst();

                // Recupera o número da coluna dos campos nome e telefone
                int colunaNome = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int colunaTelefone = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                // Recupera os campos nome e telefone
                String nome = cursor.getString(colunaNome);
                String telefone = cursor.getString(colunaTelefone);

                // Mostra os campos no LogCat
                Log.d(getPackageName(), "Nome: " + nome + "\n Telefone: " + telefone);
            }
        }
    }
}
