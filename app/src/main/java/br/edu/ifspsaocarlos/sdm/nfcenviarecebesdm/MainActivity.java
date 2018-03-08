package br.edu.ifspsaocarlos.sdm.nfcenviarecebesdm;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

}
