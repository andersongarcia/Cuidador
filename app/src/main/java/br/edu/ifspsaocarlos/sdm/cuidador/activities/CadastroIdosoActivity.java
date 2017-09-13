package br.edu.ifspsaocarlos.sdm.cuidador.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import br.edu.ifspsaocarlos.sdm.cuidador.R;
import br.edu.ifspsaocarlos.sdm.cuidador.data.CuidadorRepository;
import br.edu.ifspsaocarlos.sdm.cuidador.entities.Usuario;
import br.edu.ifspsaocarlos.sdm.cuidador.services.CuidadorService;

/**
 * Activity de cadastro de usuário
 *
 * @author Anderson Canale Garcia
 */
public class CadastroIdosoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_idoso);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_cadastro ,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.salvar:
                String nome = ((TextView)findViewById(R.id.idoso_nome)).getText().toString();
                String telefone = ((TextView)findViewById(R.id.idoso_telefone)).getText().toString();
                registrarIdoso(nome, telefone);
                Intent menuIntent = new Intent(this, MenuActivity.class);
                startActivity(menuIntent);
                break;
            case android.R.id.home:
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivity(loginIntent);
                break;
        }

        //Toast.makeText(this, msg + " clicked !", Toast.LENGTH_SHORT).show();

        return super.onOptionsItemSelected(item);
    }

    private void registrarIdoso(String nome, String telefone) {
        // busca usuário na base
        Usuario usuario = new CuidadorService(this).obterUsuarioLogado();

        CuidadorRepository usuarioDAO = new CuidadorRepository(this);
        usuarioDAO.adicionarIdoso(usuario, nome, telefone);

        // define idoso como idoso selecionado
        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.chaveIdosoSelecionado), telefone);
        editor.commit();
    }
}
