package dev.wakandaacademy.produdoro.usuario.application.service;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioApplicationServiceTest {

    @InjectMocks
    UsuarioApplicationService usuarioApplicationService;

    @Mock
    UsuarioRepository usuarioRepository;

    @Test
    void mudaStatusParaPausaLonga() {
        Usuario usuario = DataHelper.createUsuario();
        String email = usuario.getEmail();
        UUID idUsuario = usuario.getIdUsuario();
        when(usuarioRepository.salva(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        usuarioApplicationService.mudaStatusParaPausaLonga(usuario.getEmail(), usuario.getIdUsuario());
        assertEquals(StatusUsuario.PAUSA_LONGA, usuario.getStatus());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(usuarioRepository, times(1)).buscaUsuarioPorId(usuario.getIdUsuario());
        verify(usuarioRepository, times(1)).salva(usuario);
    }

    @Test
    void validaSeUsuarioJaEstaEmPausaLonga(){
        Usuario usuario = DataHelper.createUsuario();
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        usuarioApplicationService.mudaStatusParaPausaLonga(usuario.getEmail(), usuario.getIdUsuario());
        APIException exception = assertThrows(APIException.class,
                usuario::validaSeUsuarioJaEstaEmPausaLonga);
        assertEquals("usuario já está em PAUSA LONGA",
                exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST,
                exception.getStatusException());
    }

    @Test
    void deveMudarStatusParaFocoComSucesso() {
        Usuario usuario = DataHelper.createUsuario();
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario())).thenReturn(usuario);
        usuarioApplicationService.mudaStatusParaFoco(usuario.getEmail(), usuario.getIdUsuario());
        assertEquals(StatusUsuario.FOCO, usuario.getStatus());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
    }


    @Test
    void naoDeveMudarStatusParaFocoQuandoTokeninvalido() {
        Usuario usuarioLogado = DataHelper.createUsuario();
        String emailInvalido = "email@invalido.com";

        when(usuarioRepository.buscaUsuarioPorEmail(emailInvalido))
                .thenThrow(APIException.build(HttpStatus.UNAUTHORIZED, "Credencial de autenticação não é válida"));

        APIException exception = assertThrows(APIException.class,
                () -> usuarioApplicationService.mudaStatusParaFoco(emailInvalido, usuarioLogado.getIdUsuario()));

        assertEquals("Credencial de autenticação não é válida", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusException());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(emailInvalido);
        verify(usuarioRepository, never()).buscaUsuarioPorId(any());
    }
}