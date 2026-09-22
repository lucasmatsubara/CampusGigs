package br.com.fiap.campusgigs.cep;

public record ViaCepResponse(
        String cep,
        String logradouro,
        String bairro,
        String localidade,
        String uf,
        Boolean erro
) {
    public boolean isValido() {
        return erro == null || !erro;
    }
}
