package com.colorseq.abrowse;

//import com.colorseq.cscore.dao.user.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class BrowserController {

    @Value("${abrowse.default-genome}")
    private String defaultGenome;

    private String global = "global";

    private RoleEntityDao roleEntityDao;
    private UserEntityDao userEntityDao;
    private ConfigGenomeDao configGenomeDao;
    private UserConfigGenomeDao userConfigGenomeDao;

    public BrowserController() {
    }

    @Autowired
    public void setUserEntityDao(UserEntityDao userEntityDao) {
        this.userEntityDao = userEntityDao;
    }

    @Autowired
    public void setConfigGenomeDao(ConfigGenomeDao configGenomeDao) {
        this.configGenomeDao = configGenomeDao;
    }

    @Autowired
    public void setUserConfigGenomeDao(UserConfigGenomeDao userConfigGenomeDao) {
        this.userConfigGenomeDao = userConfigGenomeDao;
    }

    @Autowired
    public void setRoleEntityDao(RoleEntityDao roleEntityDao) {
        this.roleEntityDao = roleEntityDao;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Map<String, Object> modelMap, String genome, HttpSession session, Authentication authentication) {

        if (null == genome) {
            genome = defaultGenome;
        }

        int userId = 0;
        if (null != authentication) {
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            userId = userEntity.getId();

            System.out.println("登录用户ID:" + userEntity.getId());
        }

        Map<String, ConfigGenome> configGenomeMap = null;
        /*
        try {
            configGenomeMap = (Map<String, ConfigGenome>) session.getAttribute(SessionKeys.CONFIG_GENOME_MAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        if (null == configGenomeMap) {

            ConfigGenomeMapRetriever configGenomeMapRetriever = new ConfigGenomeMapRetriever();
            configGenomeMap = configGenomeMapRetriever.retrieve(userId, configGenomeDao, userConfigGenomeDao);
            session.setAttribute(SessionKeys.CONFIG_GENOME_MAP, configGenomeMap);
        }
	System.out.print("Genome:" + genome);
        ConfigGenome currentConfigGenome = configGenomeMap.get(genome);
	System.out.print("currentConfigGenome" + currentConfigGenome);
        ConfigGenome globalConfigGenome = configGenomeMap.get(global);
        if (null != globalConfigGenome) {
            currentConfigGenome.addViews(globalConfigGenome.getViewMap());
        }

        /*
        Map<String, Integer> chrNameLenMap = new HashMap<>();
        for (ConfigChromosome chr : currentConfigGenome.getChromosomeMap().values()) {
            chrNameLenMap.put(chr.getName(), chr.getLength());
        }
        modelMap.put("chrLen", chrNameLenMap);
        */

        modelMap.put("configGenome", currentConfigGenome);
        return "index";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login() {

        return "login";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(String username, String password, String repeat_password,
                           Map<String, Object> modelMap, HttpSession session)
    {

        UserEntity userEntity = this.userEntityDao.findByUsername(username);

        if (null != userEntity) {
            modelMap.put("error", "用户已存在");
            return "login";
        }

        if (! password.equals(repeat_password)) {
            modelMap.put("error", "两次密码输入不一致");
        }

        userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(password);

        /*
        RoleEntity role = new RoleEntity();
        role.setFlag(RoleFlagType.USER_FLAG);
        role.setName(RoleFlagType.ROLE_USER);
        */
        RoleEntity role = this.roleEntityDao.findByFlag(RoleFlagType.USER_FLAG);
        userEntity.addRole(role);

        this.userEntityDao.save(userEntity);

        return "redirect:/login";
    }

    @RequestMapping(value = "/about", method = RequestMethod.GET)
    public String about() {

        return "about";
    }

    @RequestMapping(value = "/config/genome", method = RequestMethod.GET)
    @ResponseBody
    public List<ConfigGenome> config_genome(String name) {

        if (null == name || name.trim().equals("")) {
            return this.configGenomeDao.findAll();
        } else {
            List<ConfigGenome> configGenomes = new ArrayList<>();
            configGenomes.add(this.configGenomeDao.findByName(name));
            return configGenomes;
        }
    }

}
