package br.com.fiap.campusgigs.cep;

import br.com.fiap.campusgigs.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class CepService {

    private final CepClient cepClient;

    public ViaCepResponse buscarEndereco(String cep) {
        String cepLimpo = somenteNumeros(cep);

        if (cepLimpo.length() != 8) {
            throw new BusinessException("CEP inválido: " + cep);
        }

        ViaCepResponse resposta;
        try {
            resposta = cepClient.buscar(cepLimpo);
        } catch (RestClientException e) {
            throw new BusinessException("Não foi possível consultar o CEP no momento. Tente novamente mais tarde.");
        }

        if (resposta == null || !resposta.isValido()) {
            throw new BusinessException("CEP não encontrado: " + cep);
        }

        return resposta;
    }

    public static String somenteNumeros(String cep) {
        return cep.replaceAll("[^0-9]", "");
    }
}
