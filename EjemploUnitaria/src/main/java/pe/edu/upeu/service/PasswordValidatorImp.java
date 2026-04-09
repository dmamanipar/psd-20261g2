package pe.edu.upeu.service;

public class PasswordValidatorImp implements PasswordValidator {
    private static final int MIN_LENGTH=8;
    private static final String ESPECIAL_CARACT="!@#$&*";

    @Override
    public boolean isValid(String passw) {
        return nivelSeguridad(passw)==5;
    }
    @Override
    public int nivelSeguridad(String passw){
        if(passw==null || passw.isEmpty()){return 0;}
        int score=0;
        if(passw.length()>=MIN_LENGTH) score++;
        if(contieneMayuscula(passw))score++;
        if(contieneMinuscula(passw))score++;
        if(contieneDigitos(passw))score++;
        if(contieneCaracterEspecial(passw))score++;
        return score;
    }

    private boolean contieneMayuscula(String s){
        for(char c:s.toCharArray()) if(Character.isUpperCase(c)) return true;
        return false;
    }
    private boolean contieneMinuscula(String s){
        for(char c:s.toCharArray()) if(Character.isLowerCase(c)) return true;
        return false;
    }
    private boolean contieneDigitos(String s){
        for(char c:s.toCharArray()) if(Character.isDigit(c)) return true;
        return false;
    }
    private boolean contieneCaracterEspecial(String s){
        for(char c:s.toCharArray()) if(ESPECIAL_CARACT.indexOf(c)>=0) return true;
        return false;
    }



}
