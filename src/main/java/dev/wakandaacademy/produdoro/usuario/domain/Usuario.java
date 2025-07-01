package dev.wakandaacademy.produdoro.usuario.domain;
import java.util.UUID;

import javax.validation.constraints.Email;

import dev.wakandaacademy.produdoro.handler.APIException;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import dev.wakandaacademy.produdoro.pomodoro.domain.ConfiguracaoPadrao;
import dev.wakandaacademy.produdoro.usuario.application.api.UsuarioNovoRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
@Document(collection = "Usuario")
public class Usuario {
	@Id
	private UUID idUsuario;
	@Email
	@Indexed(unique = true)
	private String email;
	private ConfiguracaoUsuario configuracao;
	@Builder.Default
	private StatusUsuario status = StatusUsuario.FOCO;
	@Builder.Default
	private Integer quantidadePomodorosPausaCurta = 0;

	public Usuario(UsuarioNovoRequest usuarioNovo, ConfiguracaoPadrao configuracaoPadrao) {
		this.idUsuario = UUID.randomUUID();
		this.email = usuarioNovo.getEmail();
		this.status = StatusUsuario.FOCO;
		this.configuracao = new ConfiguracaoUsuario(configuracaoPadrao);
	}
	public void pertenceAoUsuario(UUID idUsuario) {
		if (!this.idUsuario.equals(idUsuario)) {
			throw APIException.build(HttpStatus.UNAUTHORIZED,
					"Usuário(a) não autorizado(a) para a requisição solicitada! ");
		}
	}
    public void mudaStatusPausaCurta(UUID idUsuario) {
		validaUsuario(idUsuario);
		if (this.status == StatusUsuario.PAUSA_CURTA) {
			throw APIException.build(HttpStatus.BAD_REQUEST, "Usuário já está em PAUSA CURTA!");
		}
		this.status = StatusUsuario.PAUSA_CURTA;
    }

	public void mudaStatusParaPausaLonga(UUID idUsuario) {
		validaUsuario(idUsuario);
		validaSeUsuarioJaEstaEmPausaLonga();
		this.status = StatusUsuario.PAUSA_LONGA;
	}

	public void validaSeUsuarioJaEstaEmPausaLonga() {
		if (this.status.equals(StatusUsuario.PAUSA_LONGA)) {
			throw APIException.build(HttpStatus.CONFLICT,
					"usuário já está em PAUSA LONGA!");
		}
	}

	public void validaUsuario(UUID idUsuario) {
		if (!this.idUsuario.equals(idUsuario)) {
			throw APIException.build(HttpStatus.UNAUTHORIZED,
					"Credencial de Autenticação não é valida");
		}
	}


	public void tokenPertenceAoUsuario(Usuario usuarioPorEmail) {
		if (!this.idUsuario.equals(usuarioPorEmail.getIdUsuario())) {
			throw APIException.build(HttpStatus.UNAUTHORIZED, "Token Nao Corresponde ao Usuario!");
		}
	}

	public void mudaStatusParaFoco(UUID idUsuario) {
		perteceAoUsuario(idUsuario);
		verificaStatusFoco();
		alteraStatusFoco();
	}

	private void perteceAoUsuario(UUID usuarioFoco) {
		if (!this.idUsuario.equals(usuarioFoco)) {
			throw APIException.build(HttpStatus.UNAUTHORIZED, " credencial de autenticação não é válida. ");
		}

	}

	private void alteraStatusFoco() {
		this.status = StatusUsuario.FOCO;
	}

	private void verificaStatusFoco() {
		if (this.status.equals(StatusUsuario.FOCO)) {
			throw APIException.build(HttpStatus.BAD_REQUEST, "O usuário já está em foco.");
		}

	}
}