package utils;

import java.util.List;

public class EnvConfigList {
    public List<EnvConfig> config;

    // 获取环境配置url
    public String getUrl(String env) {
        for (EnvConfig cfg : config) {
            if (cfg.getEnv().equalsIgnoreCase(env)) {
                return cfg.getUrl();
            }
        }
        return null;
    }

    // 获取环境配置token
    public String getToken(String env) {
        for (EnvConfig cfg : config) {
            if (cfg.getEnv().equalsIgnoreCase(env)) {
                return cfg.getToken();
            }
        }
        return null;
    }

    // 设置环境配置url
    public void setUrl(String env, String url) {
        for (EnvConfig cfg : config) {
            if (cfg.getEnv().equalsIgnoreCase(env)) {
                cfg.setUrL(url);
                return;
            }
        }
    }

    // 设置环境配置token
    public void setToken(String env, String token) {
        for (EnvConfig cfg : config) {
            if (cfg.getEnv().equalsIgnoreCase(env)) {
                cfg.setToken(token);
                return;
            }
        }
    }

    // 返回当前环境配置
    public EnvConfig getCurrentEnv(String env) {
        for (EnvConfig cfg : config) {
            if (cfg.getEnv().equalsIgnoreCase(env)) {
                return cfg;
            }
        }
        return null;
    }
}
