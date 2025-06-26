package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.DisplayName;
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
    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void deveRetornarIdTarefaNovaCriada() {
        TarefaRequest request = getTarefaRequest();
        when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request));

        TarefaIdResponse response = tarefaApplicationService.criaNovaTarefa(request);

        assertNotNull(response);
        assertEquals(TarefaIdResponse.class, response.getClass());
        assertEquals(UUID.class, response.getIdTarefa().getClass());
    }
    @Test
    @DisplayName("Deve deletar tarefas concluídas do usuario")
    void deveDeletarTarefasConcluidasComSucesso() {
        UUID idUsuario = UUID.randomUUID();
        String email = "teste@usuario.com";

        Usuario usuarioMock = mock(Usuario.class);
        List<Tarefa> tarefasConcluidas = List.of(mock(Tarefa.class), mock(Tarefa.class));

        when(usuarioRepository.buscaUsuarioPorEmail(email)).thenReturn(usuarioMock);
        doNothing().when(usuarioMock).pertenceAoUsuario(idUsuario);
        when(tarefaRepository.buscaTarefasConcluidasPorUsuario(idUsuario)).thenReturn(tarefasConcluidas);

        tarefaApplicationService.deletaTarefasConcluidas(email, idUsuario);

        verify(tarefaRepository).deletaTarefasConcluidas(tarefasConcluidas);
    }

    @Test
    @DisplayName("Deve lançar NOT_FOUND se não houver tarefas concluídas")
    void deveLancarExcecaoSeNaoHouverTarefasConcluidas() {
        UUID idUsuario = UUID.randomUUID();
        String email = "teste@usuario.com";

        Usuario usuarioMock = mock(Usuario.class);

        when(usuarioRepository.buscaUsuarioPorEmail(email)).thenReturn(usuarioMock);
        doNothing().when(usuarioMock).pertenceAoUsuario(idUsuario);
        when(tarefaRepository.buscaTarefasConcluidasPorUsuario(idUsuario)).thenReturn(List.of());

        APIException exception = assertThrows(APIException.class, () ->
                tarefaApplicationService.deletaTarefasConcluidas(email, idUsuario)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusException());
        assertEquals("Usuário não possui nenhuma tarefa concluída!", exception.getMessage());
    }

    public TarefaRequest getTarefaRequest() {
        TarefaRequest request = new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);
        return request;
    }
}
