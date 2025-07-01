package dev.wakandaacademy.produdoro.tarefa.application.api;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/tarefa")
public interface TarefaAPI {
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    TarefaIdResponse postNovaTarefa(@RequestBody @Valid TarefaRequest tarefaRequest);

    @GetMapping("/{idTarefa}")
    @ResponseStatus(code = HttpStatus.OK)
    TarefaDetalhadoResponse detalhaTarefa(@RequestHeader(name = "Authorization", required = true) String token,
                                          @PathVariable UUID idTarefa);

    @PatchMapping("/{idTarefa}/ativa")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void ativaTarefa(@RequestHeader(name = "Authorization", required = true) String token,
                     @PathVariable UUID idTarefa);

    @DeleteMapping("/deleta-todas_tarefas/{idUsuario}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void deletaTodasTarefas(
            @RequestHeader(name = "Authorization", required = true) String token,
            @PathVariable UUID idUsuario);

    @GetMapping("/{idUsuario}/lista-tarefas")
    @ResponseStatus(code = HttpStatus.OK)
    List<TarefaDoUsuarioListResponse> listaTarefasPeloUsuario(@RequestHeader(name = "Authorization", required = true)
                                                              String token, @PathVariable UUID idUsuario);

    @PatchMapping("/{idTarefa}/editar")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void editaTarefa(@RequestHeader(name = "Authorization", required = true) String token,
                     @PathVariable UUID idTarefa,
                     @RequestBody @Valid TarefaEditaRequest request);

    @DeleteMapping("/deleta-tarefas-concluidas/{idUsuario}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void deletaTarefaConcluida(@RequestHeader(name = "Authorization", required = true) String token,
                               @PathVariable UUID idUsuario);
    @PatchMapping("/{idTarefa}/modifica-ordem")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void usuarioModificaOrdemDaTarefa(@RequestHeader(name = "Authorization", required = true) String token,
                     @PathVariable UUID idTarefa , @RequestParam (required = true, name = "posicao") int novaPosicao);

    @PatchMapping("conclui-tarefa/{idTarefa}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void concluiTarefa(@RequestHeader(name = "Authorization", required = true) String token,
                       @PathVariable UUID idTarefa);

    @PostMapping("/{idTarefa}/incrementa-pomodoro")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void patchIncrementaPomodoro(@RequestHeader(name = "Authorization", required = true) String token,
                                 @PathVariable UUID idTarefa);
}
