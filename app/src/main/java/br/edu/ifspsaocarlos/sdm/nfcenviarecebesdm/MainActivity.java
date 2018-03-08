package br.edu.ifspsaocarlos.sdm.nfcenviarecebesdm;

import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
            Toast.makeText(this, "Adaptador NFC necessário", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
