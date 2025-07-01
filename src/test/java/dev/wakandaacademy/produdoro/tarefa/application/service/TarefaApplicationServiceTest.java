package dev.wakandaacademy.produdoro.tarefa.application.service;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
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

    @Test
    void deveDeletarTodasAsTarefasDoUsuario() {
        Usuario usuario = DataHelper.createUsuario();
        List<Tarefa> tarefas = DataHelper.createListTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefasDoUsuario(usuario.getIdUsuario())).thenReturn(tarefas);

        tarefaApplicationService.limparTodasTarefas(usuario.getEmail(),
                usuario.getIdUsuario());

        verify(tarefaRepository, times(1)).deletaTodasAsTarefasDoUsuario(usuario.getIdUsuario());


    }

    public TarefaRequest getTarefaRequest() {
        TarefaRequest request = new TarefaRequest("tarefa 1", randomUUID(), null, null, 0);
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
        UUID idUsuario = randomUUID();

        when(usuarioRepository.buscaUsuarioPorId(idUsuario)).thenThrow(
                APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));


        APIException apiException = assertThrows(APIException.class, () -> tarefaApplicationService.buscaTarefasDoUsuario("briansps@gmail.com", idUsuario));

        assertEquals(HttpStatus.BAD_REQUEST, apiException.getStatusException());
        assertEquals("Usuario não encontrado!", apiException.getMessage());
    }

    @Test
    void DeveAtivarTarefaComSucesso() {
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefa = DataHelper.createTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));

        tarefaApplicationService.ativaTarefa(usuario.getEmail(), tarefa.getIdTarefa());

        assertEquals(StatusAtivacaoTarefa.ATIVA, tarefa.getStatusAtivacao());
        verify(tarefaRepository, times(1)).buscaTarefaPorId(tarefa.getIdTarefa());
    }

    @Test
    void DeveLancarExcecaoSeIdTarefaForIvalido() {
        Usuario usuario = DataHelper.createUsuario();
        UUID idTarefa = randomUUID();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(idTarefa)).thenReturn(Optional.empty());

        APIException exception = assertThrows(APIException.class,
                () -> tarefaApplicationService.ativaTarefa(usuario.getEmail(), idTarefa));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusException());
        assertEquals("id da tarefa inválido", exception.getMessage());

    }

    @Test
    void DeveLancarExcecaoSeTokenNaoPertencerAoUsuario() {
        String emailUsuario = "usuario.inexistente@teste.com";

        when(usuarioRepository.buscaUsuarioPorEmail(emailUsuario))
                .thenThrow(APIException.build(HttpStatus.UNAUTHORIZED, "Token inválido"));

        APIException exception = assertThrows(APIException.class,
                () -> tarefaApplicationService.ativaTarefa(emailUsuario, randomUUID()));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusException());
        assertEquals("Token inválido", exception.getMessage());


    }

    @Test
    @DisplayName("Deve deletar tarefas concluídas do usuario")
    void deveDeletarTarefasConcluidasComSucesso() {
        UUID idUsuario = randomUUID();
        String email = "teste@usuario.com";

        Usuario usuarioMock = mock(Usuario.class);
        List<Tarefa> tarefasConcluidas = List.of(mock(Tarefa.class), mock(Tarefa.class));

        when(usuarioRepository.buscaUsuarioPorEmail(email)).thenReturn(usuarioMock);
        doNothing().when(usuarioMock).pertenceAoUsuario(idUsuario);
        when(tarefaRepository.buscaTarefasConcluidasPorUsuario(idUsuario)).thenReturn(tarefasConcluidas);

        tarefaApplicationService.deletaTarefasConcluidas(email, idUsuario);
    }

    @Test
    @DisplayName("Deve lançar NOT_FOUND se não houver tarefas concluídas")
    void deveLancarExcecaoSeNaoHouverTarefasConcluidas() {
        UUID idUsuario = randomUUID();
        String email = "teste@usuario.com";

        Usuario usuarioMock = mock(Usuario.class);

        when(usuarioRepository.buscaUsuarioPorEmail(email)).thenReturn(usuarioMock);
        doNothing().when(usuarioMock).pertenceAoUsuario(idUsuario);
        when(tarefaRepository.buscaTarefasConcluidasPorUsuario(idUsuario)).thenReturn(List.of());

        APIException exception = assertThrows(APIException.class, () ->
                tarefaApplicationService.deletaTarefasConcluidas(email, idUsuario)
        );
    }

    void deveConcluirTarefa() {
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefa = DataHelper.createTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.salva(tarefa)).thenReturn(tarefa);
        tarefaApplicationService.concluiTarefa(usuario.getEmail(), tarefa.getIdTarefa());
    }

    @Test
    void deveLancarExcecaoSeTarefaNaoEncontrada() {
        Usuario usuario = DataHelper.createUsuario();
        UUID idInvalido = randomUUID();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        APIException exception = assertThrows(APIException.class,
                () -> tarefaApplicationService.concluiTarefa(usuario.getEmail(), idInvalido));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusException());
        verify(usuarioRepository, times(2)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(tarefaRepository, times(1)).buscaTarefaPorId(idInvalido);
        verify(tarefaRepository, never()).salva(any(Tarefa.class));
    }

    @Test
    void deveIncrementarPomodoroATarefa() {
        Usuario usuario = DataHelper.createUsuario2();
        Tarefa tarefa = DataHelper.createTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.salva(any(Tarefa.class))).thenReturn(tarefa);
        when(usuarioRepository.salva(any(Usuario.class))).thenReturn(usuario);
        tarefaApplicationService.incrementaPomodoro(usuario.getEmail(), tarefa.getIdTarefa());

        assertEquals(2, tarefa.getContagemPomodoro());
        assertEquals(StatusAtivacaoTarefa.ATIVA, tarefa.getStatusAtivacao());
    }

    @Test
    void deveLancarExcecaoSeTarefaNaoExisteAoIncrementarPomodoro() {
        UUID idInvalido = randomUUID();
        Usuario usuario = DataHelper.createUsuario2();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(idInvalido)).thenReturn(Optional.empty());

        APIException exception = assertThrows(APIException.class, () ->
                tarefaApplicationService.incrementaPomodoro(usuario.getEmail(), idInvalido));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusException());
        assertEquals("Tarefa não encontrada!", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoSeUsuarioNaoPertencerATarefaAoIncrementarPomodoro() {
        Tarefa tarefa = DataHelper.createTarefa();
        Usuario usuario = DataHelper.createUsuario2();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));

        APIException exception = assertThrows(APIException.class, () ->
                tarefaApplicationService.incrementaPomodoro(usuario.getEmail(), tarefa.getIdTarefa()) );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusException());
        assertEquals("Usuário não é dono da Tarefa solicitada!", exception.getMessage());
    }
}