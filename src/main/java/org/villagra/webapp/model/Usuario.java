package org.villagra.webapp.model;

public class Usuario {

    String name;
    String email;
    String password;
    String salt;
    String token;

    public Usuario() {

    }
    public Usuario(String nome, String email, String clave,String salt) {
        this.name=nome;
        this.email=email;
        this.password=clave;
        this.salt=salt;


    }
    public Usuario(String nome, String email, String clave,String salt,String token) {
        this.name=nome;
        this.email=email;
        this.password=clave;
        this.salt=salt;
        this.token=token;

    }
    public String getNome() {
        return name;
    }
    public void setNome(String nome) {
        this.name = nome;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getSalt() {
        return salt;
    }
    public void setSalt(String salt) {
        this.salt = salt;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

}
