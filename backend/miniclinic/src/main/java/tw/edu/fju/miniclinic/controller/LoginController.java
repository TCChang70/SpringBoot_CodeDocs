package tw.edu.fju.miniclinic.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import tw.edu.fju.miniclinic.model.*;

@Controller
public class LoginController {

    @Autowired
    private DoctorRepository doctorRepo;

    // ==================== 1. 登入功能 ====================
    // GET：顯示登入頁
    @GetMapping("/login")
    public String loginForm(Model model) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        return "login";
    }

    // POST：處理登入
    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginForm") LoginForm form,
            BindingResult result,
            HttpSession session,
            Model model) {

        // 步驟 1：檢查表單驗證
        if (result.hasErrors()) {
            return "login";  // 顯示錯誤訊息
        }

        // 步驟 2：查詢醫師
        Doctor doctor = doctorRepo.findById(form.getDoctorId()).orElse(null);

        // 步驟 3：檢查密碼（醫師不存在或密碼錯都給同樣的錯誤訊息，避免洩漏帳號是否存在）
        if (doctor == null || !BCrypt.checkpw(form.getPassword(), doctor.getPasswordHash())) {
            model.addAttribute("errorMessage", "醫師編號或密碼錯誤");
            return "login";
        }

        // 步驟 4：登入成功，存入 Session
        session.setAttribute("loggedInDoctorId", doctor.getDoctorId());
        session.setAttribute("loggedInDoctorName", doctor.getName());

        return "redirect:/dashboard";
    }

    // ==================== 2. 登出功能 ====================
    // 登出
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // 清除 Session
        return "redirect:/login";
    }

    // ==================== 🌟 3. 修改密碼功能 (Part B 要求) ====================
    // GET：顯示修改密碼畫面
    @GetMapping("/password")
    public String showPasswordForm(HttpSession session, Model model) {
        // 將目前登入的醫師姓名放入 Model 渲染到網頁畫面上
        String doctorName = (String) session.getAttribute("loggedInDoctorName");
        model.addAttribute("loggedInDoctorName", doctorName);
        
        // 提供一個空表單物件給 Thymeleaf th:object 綁定
        model.addAttribute("passwordForm", new PasswordForm());
        return "password";
    }

    // POST：處理修改密碼邏輯與 4 大資安防線
    @PostMapping("/password")
    public String processChangePassword(
            @ModelAttribute("passwordForm") PasswordForm form,
            HttpSession session,
            Model model) {
        
        // 每次進來都要重新把姓名塞給 Model，防止畫面渲染時因空白報錯
        String doctorName = (String) session.getAttribute("loggedInDoctorName");
        model.addAttribute("loggedInDoctorName", doctorName);

        // 核心邏輯 1：從 Session 撈出目前登入的醫師 ID 並到 DB 找資料
        String doctorId = (String) session.getAttribute("loggedInDoctorId");
        Doctor doctor = doctorRepo.findById(doctorId).orElse(null);

        if (doctor == null) {
            return "redirect:/login"; // 異常狀況直接踢回登入頁面
        }

        // 核心邏輯 2：驗證舊密碼是否正確（使用 BCrypt 比對資料庫內雜湊值）
        if (!BCrypt.checkpw(form.getOldPassword(), doctor.getPasswordHash())) {
            model.addAttribute("errorMessage", "舊密碼錯誤");
            return "password";
        }

        // 核心邏輯 3：驗證新密碼與確認新密碼是否一致
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("errorMessage", "兩次密碼不相符");
            return "password";
        }

        // 核心邏輯 4：驗證新密碼長度（至少需要 8 碼）
        if (form.getNewPassword() == null || form.getNewPassword().length() < 8) {
            model.addAttribute("errorMessage", "密碼至少需要 8 個字元");
            return "password";
        }

        // 🌟 通過所有安全防線：為新密碼打上 BCrypt 加密、更新資料庫
        String hashedNewPassword = BCrypt.hashpw(form.getNewPassword(), BCrypt.gensalt());
        doctor.setPasswordHash(hashedNewPassword);
        doctorRepo.save(doctor);

        // 🌟 貼心小體貼：修改成功後，塞入一個乾淨、空白的 Form，把網頁上的密碼框通通清空
        model.addAttribute("passwordForm", new PasswordForm());
        model.addAttribute("successMessage", "密碼修改成功！");
        return "password";
    }
}