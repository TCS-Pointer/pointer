package br.com.pointer.pointer_back.dto;

public class UsuarioResponseDTO {
    private Long id;
    private String nome;
    private String matricula;
    private String email;
    private String cpf;
    private String cargo;
    private String setor;
    private boolean status;

    public UsuarioResponseDTO() {}

    public UsuarioResponseDTO(Long id, String nome, String matricula, String email, String cpf, String cargo, String setor, boolean status) {
        this.id = id;
        this.nome = nome;
        this.matricula = matricula;
        this.email = email;
        this.cpf = cpf;
        this.cargo = cargo;
        this.setor = setor;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getSetor() { return setor; }
    public void setSetor(String setor) { this.setor = setor; }

    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
}
