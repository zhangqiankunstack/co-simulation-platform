package com.rengu.cosimulation.service;

import com.rengu.cosimulation.entity.*;
import com.rengu.cosimulation.enums.ResultCode;
import com.rengu.cosimulation.exception.ResultException;
import com.rengu.cosimulation.repository.*;
import com.rengu.cosimulation.utils.ExcelUtils;
import com.rengu.cosimulation.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Author: XYmar
 * Date: 2019/2/12 16:15
 */
@Service
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final DepartmentService departmentService;
    private final ProjectRepository projectRepository;
    private final DeviceRepository deviceRepository;

    private final SubtaskRepository subtaskRepository;
    private final SublibraryFilesRepository sublibraryFilesRepository;

    @Autowired
    public UserService(UserRepository userRepository, RoleService roleService, DepartmentService departmentService, ProjectRepository projectRepository, SubtaskRepository subtaskRepository, SublibraryFilesRepository sublibraryFilesRepository,
                       DeviceRepository deviceRepository) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.departmentService = departmentService;
        this.projectRepository = projectRepository;
        this.subtaskRepository = subtaskRepository;
        this.sublibraryFilesRepository = sublibraryFilesRepository;
        this.deviceRepository = deviceRepository;
    }

    // 保存用户 , 一个角色
    @CacheEvict(value = "User_Cache", allEntries = true)
    public Users saveUser(String departmentName, Users users, String roleName) {
        if (users == null) {
            throw new ResultException(ResultCode.USER_ARGS_NOT_FOUND_ERROR);
        }
        if (StringUtils.isEmpty(users.getUsername())) {
            throw new ResultException(ResultCode.USER_USERNAME_NOT_FOUND_ERROR);
        }
        if (StringUtils.isEmpty(users.getRealName())) {
            throw new ResultException(ResultCode.USER_REALNAME_NOT_FOUND_ERROR);
        }
        if (hasUserByUsername(users.getUsername())) {
            throw new ResultException(ResultCode.USER_USERNAME_EXISTED_ERROR);
        }
        if (StringUtils.isEmpty(String.valueOf(users.getSecretClass()))) {
            throw new ResultException(ResultCode.USER_SECRETCLASS_NOT_FOUND_ERROR);
        }
        users.setPassword(new BCryptPasswordEncoder().encode("123456"));

        if (StringUtils.isEmpty(roleName)) {
            throw new ResultException(ResultCode.USER_ROLE_NOT_FOUND_ERROR);
        }
        Set<Role> roleSet = new HashSet<>();
        roleSet.add(roleService.getRoleByName(roleName));
        users.setRoleEntities(roleSet);
        if (StringUtils.isEmpty(String.valueOf(users.getSecretClass()))) {
            users.setSecretClass(0);
        }

        if (!departmentService.hasDepartmentByName(departmentName)) {
            throw new ResultException(ResultCode.USER_DEPARTMENT_ARGS_NOT_FOUND_ERROR);
        }
        Department department = departmentService.getDepartmentByName(departmentName);
        users.setDepartment(department);
        return userRepository.save(users);
    }

    // 导入用户
    public List<Users> importUser(String type, MultipartFile multipartFile) throws Exception {
        File tempFile = new File(FileUtils.getTempDirectoryPath() + "/" + multipartFile.getOriginalFilename());
        tempFile.getParentFile().mkdirs();          //得到Ecael文件名的父类，并建立整个父类目录
        tempFile.createNewFile();                   //然后创建一个新的文件
        IOUtils.copy(multipartFile.getInputStream(), new FileOutputStream(tempFile));//相当于一个文件可以复制到另一个文件中
        switch (type) {
            case "EXCEL":
                List<Users> lists = importUserByExcel(tempFile);
                List<Users>  rr = userRepository.saveAll(lists);
                System.out.println("文件总条数："+rr.size());

                return rr;
            default:
        }
        return new ArrayList<>();
    }

    private List<Users> importUserByExcel(File exceFile) throws Exception {
        // todo 1.解析excel文件，获取用户和部门条目
        // todo 2.读取部门和用户条目，创建Users Department list
        // todo 3.将users Department list遍历保存进数据库（注意，需要判断用户信息、部门名称是否已经存在，若存在需要跳过）
        //文件
        Map<String, String> m = new HashMap<>();
        //m.put("ID", "id");
        m.put("EMPID", "empId");
        m.put("USERID", "userId");
        m.put("EMPNAME", "empName");
        m.put("CARDNO", "cardNo");
        m.put("SECRETRANK", "secretRank");
        m.put("ORGNAME", "orgName");
        //m.put("EMPID1", "enpId1");
        m.put("POSINAME", "posiName");

        List<Map<String, Object>> map = ExcelUtils.parseExcel(exceFile, m);
        ArrayList<Users> listUsers = new ArrayList<>();
        for (Map<String, Object> lUsers : map) {
            Users users = new Users();
            users.setEmpId((String) lUsers.get("empId"));
            users.setUserId((String) lUsers.get("userId"));
            users.setRealName((String) lUsers.get("empName"));
            users.setUsername((String) lUsers.get("cardNo"));
            users.setSecredRank((String)lUsers.get("secretRank"));
            users.setOrgName((String) lUsers.get("orgName"));
            users.setPosiName((String) lUsers.get("posiName"));
            String i = "$2a$10$KB9r2GqPPS1dSByUmHkN1Om3YTcTUxa4rwAREm1SIm8uBBLPo.7ku";
            users.setPassword(i);
            //System.out.println("密码"+new BCryptPasswordEncoder().encode("123456"));
            String roleName = "normal_designer";
            Set<Role> role_Set = new HashSet<>();
            role_Set.add(roleService.getRoleByName(roleName));
            users.setRoleEntities(role_Set);
            listUsers.add(users);
        }
        // 去重
        Set<String> set = new HashSet<>();
        List<Users> newList = new ArrayList<>();
        for (Users u : listUsers) {
            if (!set.contains(u.getUsername())) {
                set.add(u.getUsername());
                newList.add(u);
            }
        }

        return newList;
    }

    // 查询所有用户
    public List<Users> getAll() {
        return userRepository.findByDeleted(false);
    }

    // 根据用户名查询用户是否存在
    public boolean hasUserByUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            return false;
        }
        return userRepository.existsByUsernameAndDeleted(username, false);
    }

    // 根据用户名查询用户
    @Cacheable(value = "User_Cache", key = "#username")
    public Users getUserByUsername(String username) {
        if (!hasUserByUsername(username)) {
            throw new ResultException(ResultCode.USER_USERNAME_NOT_FOUND_ERROR);
        }
        return userRepository.findByUsernameAndDeleted(username,false);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUserByUsername(username);
    }


    // 根据Id查询用户是否存在
    public boolean hasUserById(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return false;
        }
        return userRepository.existsById(userId);
    }

    // 根据id查询用户
    @Cacheable(value = "User_Cache", key = "#userId")
    public Users getUserById(String userId) {
        if (!hasUserById(userId)) {
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        return userRepository.findById(userId).get();
    }

    // 根据id修改用户
    @CachePut(value = "User_Cache", key = "#userId")
    public Users updateUserByUserId(String userId, Users usersArgs) {
        if (!hasUserById(userId)) {
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        Users users = getUserById(userId);
        if (usersArgs == null) {
            throw new ResultException(ResultCode.USER_ARGS_NOT_FOUND_ERROR);
        }

        if (!StringUtils.isEmpty(usersArgs.getUsername()) && !users.getUsername().equals(usersArgs.getUsername())) {
            if (hasUserByUsername(usersArgs.getUsername())) {
                throw new ResultException(ResultCode.USER_USERNAME_EXISTED_ERROR);
            }
            users.setUsername(usersArgs.getUsername());
        }

        if (!StringUtils.isEmpty(usersArgs.getRealName()) && !users.getRealName().equals(usersArgs.getRealName())) {
            users.setRealName(usersArgs.getRealName());
        }

        return userRepository.save(users);
    }

    // 根据id删除用户
    @CacheEvict(value = "User_Cache", key = "#userId")
    public Users deleteByUserId(String userId) {
        if (!hasUserById(userId)) {
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        Users users = getUserById(userId);
        List<SubDepotFile> subDepotFiles = sublibraryFilesRepository.findByUsers(users);
        if (subDepotFiles.size() > 0) {             // 用户为子库文件上传者时则假删除，否则直接删除
            users.setEnabled(false);
            users.setDeleted(true);
            userRepository.save(users);
        } else {
            List<Project> projectList = projectRepository.findByPicOrCreator(users, users);
            if (projectList.size() > 0) {
                for (Project project : projectList) {
                    if (project.getPic().getId().equals(userId)) {
                        project.setPic(null);
                    } else {
                        project.setCreator(null);
                    }
                }
            }
            List<Subtask> subtaskList = subtaskRepository.findByUsersOrProofSetContainingOrAuditSetContainingOrCountSetContainingOrApproveSetContaining(users, users, users, users, users);
            if (subtaskList.size() > 0) {
                for (Subtask subtask : subtaskList) {
                    if (subtask.getUsers().getId().equals(userId)) {
                        subtask.setUsers(null);
                    }
                    subtask.getProofSet().remove(users);
                    subtask.getAuditSet().remove(users);
                    subtask.getCountSet().remove(users);
                    subtask.getApproveSet().remove(users);
                }
                subtaskRepository.saveAll(subtaskList);
            }
            List<SubDepotFile> subDepotFileList = sublibraryFilesRepository.findByProofSetContainingOrAuditSetContainingOrCountSetContainingOrApproveSetContaining(users, users, users, users);
            if (subDepotFileList.size() > 0) {
                for (SubDepotFile subDepotFile : subDepotFileList) {
                    subDepotFile.getProofSet().remove(users);
                    subDepotFile.getAuditSet().remove(users);
                    subDepotFile.getCountSet().remove(users);
                    subDepotFile.getApproveSet().remove(users);
                }
                sublibraryFilesRepository.saveAll(subDepotFileList);
            }
            userRepository.delete(users);
        }
        return users;
    }

    // 根据id修改用户密码
    @CacheEvict(value = "User_Cache", key = "#userId")
    public Users updatePasswordById(String userId, String password) {
        if (!hasUserById(userId)) {
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        if (StringUtils.isEmpty(password)) {
            throw new ResultException(ResultCode.USER_PASSWORD_ARGS_NOT_FOUND_ERROR);
        }
        Users users = getUserById(userId);
        users.setPassword(new BCryptPasswordEncoder().encode(password));
        return userRepository.save(users);
    }

    // 根据id修改用户所属部门
    public Users updateDepartmentById(String userId, String departmentId) {
        if (!hasUserById(userId)) {
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        Users users = getUserById(userId);
        if (!departmentService.hasDepartmentById(departmentId)) {
            throw new ResultException(ResultCode.DEPARTMENT_ID_NOT_FOUND_ERROR);
        }
        Department department = departmentService.getDepartmentById(departmentId);
        users.setDepartment(department);
        return userRepository.save(users);
    }

    // 安全保密员修改用户密级
    @CacheEvict(value = "User_Cache", key = "#userId")
    public Users updateSecretClassById(String userId, int secretClass) {
        if (!hasUserById(userId)) {
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        if (StringUtils.isEmpty(secretClass)) {
            throw new ResultException(ResultCode.USER_SECRETCLASS_NOT_FOUND_ERROR);
        }
        Users users = getUserById(userId);
        users.setSecretClass(secretClass);
        return userRepository.save(users);
    }

    // 根据id分配用户权限,  一次只能分配一个角色, 再次传入角色的时候累加
    @CacheEvict(value = "User_Cache", key = "#userId")
    public Users distributeUserById(String userId, String[] ids) {
        if (!hasUserById(userId)) {
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        Users users = getUserById(userId);

        List<Role> roleList = new ArrayList<>();
        if (ids.length == 0) {
            throw new ResultException(ResultCode.USER_ROLE_NOT_FOUND_ERROR);
        }
        for (String id : ids) {
            roleList.add(roleService.getRoleById(id));
        }

        HashSet<Role> roleHashSet = new HashSet<>(roleList);

        users.setRoleEntities(roleHashSet);
        return userRepository.save(users);
    }

    // 根据id禁用或解除禁用
    @CacheEvict(value = "User_Cache", key = "#userId")
    public Users assignUserById(String userId, Boolean enabled) {
        if (!hasUserById(userId)) {
            throw new ResultException(ResultCode.USER_ID_NOT_FOUND_ERROR);
        }
        if (StringUtils.isEmpty(enabled)) {
            throw new ResultException(ResultCode.USER_ENABLED_NOT_SUPPORT_ERROR);
        }
        Users users = getUserById(userId);
        users.setEnabled(enabled);
        return userRepository.save(users);

        }

    }
