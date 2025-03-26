package co.edu.upb.app.config;

import io.github.cdimascio.dotenv.Dotenv;

public class Environment {

    private static volatile Environment instance;

    private Dotenv dotenv;

    private Environment(){
        this.dotenv = Dotenv.load();
    }

    public static Environment getInstance(){
        if(instance == null){
            synchronized (Environment.class){
                if(instance==null){
                    instance = new Environment();
                }
            }
        }
        return instance;
    }

    public Dotenv getDotenv(){
        return this.dotenv;
    }
}
