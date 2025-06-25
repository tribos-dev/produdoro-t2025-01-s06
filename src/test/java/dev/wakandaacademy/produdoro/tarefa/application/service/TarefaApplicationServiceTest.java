package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class TarefaApplicationServiceTest {

    //	@Autowired
    @InjectMocks
    TarefaApplicationService tarefaApplicationService;

    //	@MockBean
    @Mock
    TarefaRepository tarefaRepository;
    //	@MockBean
    @Mock
    UsuarioRepository usuarioRepository;

    @Test
    void deveRetornarIdTarefaNovaCriada() {
        TarefaRequest request = getTarefaRequest();
        when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request));

        TarefaIdResponse response = tarefaApplicationService.criaNovaTarefa(request);

        assertNotNull(response);
        assertEquals(TarefaIdResponse.class, response.getClass());
        assertEquals(UUID.class, response.getIdTarefa().getClass());
    }


    public TarefaRequest getTarefaRequest() {
        TarefaRequest request = new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);
        return request;
    }

    @Test
    void deveRetornarListaTarefasDoUsuario() {
        List<Tarefa> listTarefa = DataHelper.createListTarefa();
        Usuario usuario = DataHelper.createUsuario();

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefasDoUsuario(any())).thenReturn(listTarefa);

        tarefaApplicationService.buscaTarefasDoUsuario(usuario.getEmail(), usuario.getIdUsuario());

        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(any());
        verify(usuarioRepository, times(1)).buscaUsuarioPorId(any());
        verify(tarefaRepository, times(1)).buscaTarefasDoUsuario(any());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontradoAoListarTarefas() {
        UUID idUsuario = UUID.randomUUID();

        when(usuarioRepository.buscaUsuarioPorId(idUsuario)).thenThrow(
                APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));


        APIException apiException = assertThrows(APIException.class, () -> tarefaApplicationService.buscaTarefasDoUsuario("briansps@gmail.com", idUsuario));

        assertEquals(HttpStatus.BAD_REQUEST, apiException.getStatusException());
        assertEquals("Usuario não encontrado!", apiException.getMessage());
    }
}
