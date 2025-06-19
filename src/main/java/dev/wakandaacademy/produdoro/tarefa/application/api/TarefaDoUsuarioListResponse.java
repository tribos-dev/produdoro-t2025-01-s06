package dev.wakandaacademy.produdoro.tarefa.application.api;

import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TarefaDoUsuarioListResponse {
    private UUID idTarefa;
    private String descricao;
    private UUID idUsuario;
    private StatusTarefa status;
    private StatusAtivacaoTarefa statusAtivacao;
    private int contagemPomodoro;

}
