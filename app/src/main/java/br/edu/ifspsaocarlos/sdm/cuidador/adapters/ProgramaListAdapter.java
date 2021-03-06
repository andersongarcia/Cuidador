package br.edu.ifspsaocarlos.sdm.cuidador.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import br.edu.ifspsaocarlos.sdm.cuidador.R;
import br.edu.ifspsaocarlos.sdm.cuidador.entities.Programa;
import br.edu.ifspsaocarlos.sdm.cuidador.interfaces.RecyclerViewOnItemSelecionado;
import br.edu.ifspsaocarlos.sdm.cuidador.repositories.ProgramasRepository;

/**
 * Adapter para lista de programas favoritos
 *
 * @author Anderson Canale Garcia
 */
public class ProgramaListAdapter extends RecyclerView.Adapter<ProgramaListAdapter.ProgramaHolder> {
    private ProgramasRepository repositorio;
    private List<Programa> lista;
    private LayoutInflater mLayoutInflater;
    private RecyclerViewOnItemSelecionado meuRecyclerViewOnItemSelecionado;
    private View emptyView;

    // Observer pattern
    private Observer observer = new Observer() {
        /**
         * Quando notificado, atualiza RecyclerView
         * @param observable objeto a ser observado
         * @param o não utilizado
         */
        @Override
        public void update(Observable observable, Object o) {
            notifyDataSetChanged();
            checkIfEmpty();
        }
    };

    public ProgramaListAdapter(Context c, String idosoId) {
        repositorio = ProgramasRepository.getInstance();
        lista = repositorio.getProgramas();
        mLayoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // define observer
        repositorio.addObserver(observer);
        // carrega lista de programas
        repositorio.carregaProgramas(idosoId, null);
    }

    @Override
    public ProgramaListAdapter.ProgramaHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = mLayoutInflater.inflate(R.layout.item_lista, parent, false);

        return new ProgramaHolder(v);
    }

    @Override
    public void onBindViewHolder(ProgramaListAdapter.ProgramaHolder holder, int position) {

        holder.tvNome.setText(lista.get(position).getNome());
        holder.tvHorarios.setText(lista.get(position).getHorario());
    }

    @Override
    public int getItemCount() {

        return lista.size();
    }

    public Programa getItem(int posicao) {
        return lista.get(posicao);
    }

    public void setRecyclerViewOnItemSelecionado(RecyclerViewOnItemSelecionado r){

        meuRecyclerViewOnItemSelecionado = r;
    }

    public void setEmptyView(View view){
        emptyView = view;
    }

    private void checkIfEmpty() {
        if(getItemCount() > 0){
            emptyView.setVisibility(View.GONE);
        }else {
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    public class ProgramaHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvNome;
        TextView tvHorarios;

        ProgramaHolder(View itemView) {

            super(itemView);

            tvNome = (TextView) itemView.findViewById(R.id.lista_titulo);
            tvHorarios = (TextView) itemView.findViewById(R.id.lista_descricao);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(meuRecyclerViewOnItemSelecionado != null){
                meuRecyclerViewOnItemSelecionado.onItemSelecionado(v, getAdapterPosition());
            }
        }
    }
}
