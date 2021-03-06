package br.edu.ifspsaocarlos.sdm.cuidador.repositories;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import br.edu.ifspsaocarlos.sdm.cuidador.entities.Remedio;
import br.edu.ifspsaocarlos.sdm.cuidador.enums.AlertaRemedio;
import br.edu.ifspsaocarlos.sdm.cuidador.enums.NO;
import br.edu.ifspsaocarlos.sdm.cuidador.services.AlarmeService;

/**
 * Repositório de acesso a dados dos remédios
 *
 * @author Anderson Canale Garcia
 */public class RemediosRepository extends Observable {
    private static final String TAG = "RemediosRepository";

    private static RemediosRepository repository;
    private final DatabaseReference remedioEndPoint;
    private final DatabaseReference alertaRemedioEndPoint;
    private List<Remedio> remedios;

    private RemediosRepository() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        remedioEndPoint = firebaseDatabase.getReference().child(NO.getNo(NO.REMEDIOS));
        alertaRemedioEndPoint = firebaseDatabase.getReference().child(NO.getNo(NO.ALERTA_REMEDIO));
        remedios = new ArrayList<>();
    }

    // Singleton
    public static RemediosRepository getInstance(){
        if(repository == null){
            repository = new RemediosRepository();
        }

        return repository;
    }

    public List<Remedio> getRemedios() {
        return remedios;
    }


    private String adicionaRemedio(String idosoId, Remedio remedio) {
        FirebaseRepository firebaseRepository = FirebaseRepository.getInstance();
        DatabaseReference reference = remedioEndPoint.child(idosoId);

        // cria identificador
        String key = reference.push().getKey();
        remedio.setId(key);

        // pega código para alarme
        remedio.setCodigoAlarme(firebaseRepository.incrementaContadorAlarme(idosoId));

        reference.child(key).setValue(remedio);

        remedios.add(remedio);

        return key;
    }

    private void atualizaRemedio(String idosoId, Remedio remedio) {
        remedioEndPoint.child(idosoId).child(remedio.getId()).setValue(remedio);
    }

    public void removeRemedio(String idosoId, String remedioId) {
        remedioEndPoint.child(idosoId).child(remedioId).removeValue();
        removeRemedioDaLista(remedioId);
    }

    /**
     * Carrega lista de remédios cadastrados para um idoso
     * @param idosoId Id do idoso
     * @param alarmeService Serviço de alarmes, quando necessário reagendamento
     */
    public void carregaRemedios(String idosoId, final AlarmeService alarmeService) {
        // A partir da referência para o nó do idoso, abaixo do nó de remédios,
        // cria listener para ler eventos de atualizações dos dados
        remedioEndPoint.child(idosoId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                remedios.clear();   // limpa lista de remédios
                // Adiciona cada nó filho como remédio na lista
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    // Converte dados do snapshot para objeto de domínio
                    Remedio remedio = postSnapshot.getValue(Remedio.class);
                    remedios.add(remedio);  // adiciona remédio à lista

                    // Se houver serviço de alarme definido,
                    if(alarmeService != null){
                        alarmeService.atualizaAlarmeRemedio(remedio);
                    }
                }
                // Notifica observers que houve atualização na lista
                notificar();
            }

            // Em caso de falha, listener é cancelado
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getMessage());
            }
        });
    }

    private void removeRemedioDaLista(String remedioId) {
        Iterator<Remedio> i = remedios.iterator();
        while (i.hasNext()) {
            Remedio remedio = i.next();
            if(remedio.getId() == remedioId)
                i.remove();
        }
        notificar();
    }


    public String salvaRemedio(String idosoId, Remedio remedio) {
        String id = remedio.getId();
        if(remedio.getId() == null || remedio.getId().isEmpty()){
            id = adicionaRemedio(idosoId, remedio);
        }else {
            atualizaRemedio(idosoId, remedio);
        }

        notificar();

        return id;
    }

    private void notificar() {
        setChanged();
        notifyObservers();
    }

    public Remedio obterRemedio(String remedioId) {
        for (Remedio remedio : remedios) {
            if (remedio.getId().equals(remedioId))
                return remedio;
        }

        return null;
    }

    public void salvaAlertaRemedio(AlertaRemedio alerta, String idosoId, String remedioId) {
        alertaRemedioEndPoint.child(idosoId).child(remedioId).child(AlertaRemedio.getString(alerta)).setValue(FirebaseRepository.getTimestampNow());
    }

    public void salvaUriInstrucao(String idosoId, String id, String uri) {
        remedioEndPoint.child(idosoId).child(id).child(NO.getNo(NO.INSTRUCAO_URI)).setValue(uri);
    }

    public void confirmarHorario(final String idosoId, final String remedioId, String horaMedicacao, String proximaMedicacao) {
        remedioEndPoint.child(idosoId).child(remedioId).child(NO.getNo(NO.HORARIO)).setValue(proximaMedicacao);
        alertaRemedioEndPoint.child(idosoId).child(remedioId).child(NO.getNo(NO.HORARIO)).setValue(horaMedicacao)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        salvaAlertaRemedio(AlertaRemedio.CONFIRMACAO_CUIDADOR, idosoId, remedioId);
                    }
                });
    }
}