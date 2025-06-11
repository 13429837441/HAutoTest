package utils;

import java.util.List;
import java.util.Optional;

public class VarConfigList {
    public List<VarConfig> config;

    // 根据变量名获取变量值
    public String getVarValue(String varName) {
        System.out.println(config);
        for (VarConfig cfg : config) {
            if (cfg.getName().equals(varName)) {
                return cfg.getValue();
            }
        }
        return null;
    }

    // 根据变量名设置变量值
    public void setVarValue(String varName, String value, Optional<String> desc) {
        boolean exist = false;
        for (VarConfig cfg : config) {
            if (cfg.getName().equals(varName)) {
                desc.ifPresent(cfg::setDescription);
                cfg.setValue(value);
                exist = true;
                break;
            }
        }
        if (!exist) {
            VarConfig cfg = new VarConfig();
            cfg.setName(value);
            desc.ifPresent(cfg::setDescription);
        }
    }

    // 获取所有变量
    public List<VarConfig> getAllVariables() {
        return config;
    }

}
