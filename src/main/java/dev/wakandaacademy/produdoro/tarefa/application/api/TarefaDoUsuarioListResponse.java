package dev.wakandaacademy.produdoro.tarefa.application.api;

import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class TarefaDoUsuarioListResponse {
    private UUID idTarefa;
    private String descricao;
    private UUID idUsuario;
    private StatusTarefa status;
    private StatusAtivacaoTarefa statusAtivacao;
    private int contagemPomodoro;

    public static List<TarefaDoUsuarioListResponse> converte(List<Tarefa> tarefas) {
        return tarefas.stream()
                .map(TarefaDoUsuarioListResponse::new)
                .collect(Collectors.toList());
    }
    public TarefaDoUsuarioListResponse(Tarefa tarefa) {
        this.idTarefa = tarefa.getIdTarefa();
        this.descricao = tarefa.getDescricao();
        this.idUsuario = tarefa.getIdUsuario();
        this.status = tarefa.getStatus();
        this.statusAtivacao = tarefa.getStatusAtivacao();
        this.contagemPomodoro = tarefa.getContagemPomodoro();
    }
}
