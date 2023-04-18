package com.itwillbs.spring_mvc_board_kyr.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.itwillbs.spring_mvc_board_kyr.service.MemberService;
import com.itwillbs.spring_mvc_board_kyr.vo.MemberVO;

@Controller
public class MemberController {
	
	@Autowired
	private MemberService service;
	
	// "/MemberJoinForm.me" 요청에 대해 "member/member_join_form.jsp" 페이지 포워딩
	// => GET 방식 요청, Dispatch 방식 포워딩
	@GetMapping(value = "/MemberJoinForm.me")
	public String joinForm() {
		return "member/member_join_form";
	}
	
	// "/MemberJoinPro.me" 요청에 대해 MemberService 객체 비즈니스 로직 수행
	// => POST 방식 요청, Redirect 방식
	// => 폼 파라미터로 전달되는 가입 정보를 파라미터로 전달받기
	// => 가입 완료 후 이동할 페이지 : 메인페이지(index.jsp)
	// => 가입 실패 시 오류 페이지(fail_back)를 통해 "회원 가입 실패!" 출력 후 이전페이지로 돌아가기
	@PostMapping(value = "/MemberJoinPro.me")
	public String joinPro(MemberVO member, Model model) {
		System.out.println(member);
		// ------------ BCryptPasswordEncoder 객체 활용한 패스워드 암호화(해싱) -------------
		// 입력받은 패스워드는 암호화(해싱) 필요 => 해싱 후 MemberVO 객체 패스워드에 덮어쓰기
		// => 스프링에서 암호화는 org.springframework.security.crypto.bcrypt 패키지의
		//    BCryptPasswordEncoder 클래스 활용(spring-security-web 라이브러리 추가 필요)
		// => BCryptPasswordEncoder 클래스 활용하여 해싱할 경우 Salting(솔팅) 기능을 통해
		//    동일한 평문(원본 암호)이라도 매번 다른 결과값을 갖는 해싱이 가능하다!
		// 1. BCryptPasswordEncoder 객체 생성
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		// 2. BCryptPasswordEncoder 객체의 encode() 메서드를 호출하여 해싱 후 결과값 리턴
		// => 파라미터 : 평문 암호    리턴타입 : String(해싱된 암호)
		String securePasswd = passwordEncoder.encode(member.getPasswd());
//		System.out.println("평문 : " + member.getPasswd()); // 평문 암호 출력
//		System.out.println("암호문 : " + securePasswd); // 해싱된 암호 출력
		// 3. MemberVO 객체의 패스워드에 암호화 된 패스워드 저장(덮어쓰기)
		member.setPasswd(securePasswd);
		// ----------------------------------------------------------------------------------
		// MemberService - registMember() 메서드 호출을 통해 회원 가입 작업 요청
		// => 파라미터 : MemberVO 객체    리턴타입 : int
		int insertCount = service.registMember(member);
		
		if(insertCount > 0) { // 가입 성공
			// 리다이렉트 방식으로 "/" 요청(HomeController - index.jsp)
			return "redirect:/";
		} else { // 가입 실패
			// Model 객체의 "msg" 속성으로 "회원 가입 실패!" 문자열 저장 후 fail_back.jsp 포워딩
			model.addAttribute("msg", "회원 가입 실패!");
			return "fail_back";
		}
		
	}
	@GetMapping(value = "/MemberLoginForm.me")
	public String loginForm() {
		return "member/member_login_form";
	}
	
	@PostMapping(value="/MemberLoginPro.me")
	public String LoginPro(MemberVO member, Model model, HttpSession session) {
//		memberService.checkUser(id,passwd);
		//해싱 
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//		String securePasswd = passwordEncoder.encode(member.getPasswd());
//		member.setPasswd(securePasswd);
//		System.out.println("비밀번호" + member.getPasswd());
		
		String passwd = service.getPasswd(member.getId());
		System.out.println(passwd);
		
		//DB로부터 조회된 기존 패스워드를 입력받은 패스워드와 비교 하여 로그인 성공여부 판별 matches()메서드 활용
		
		// 1) 입력받은 아이디가 존재하지 않을경우 패스워드값이 null 실패 
		// 2) 입력받은 아이디가 존재활 경우 
		// 2-1) 두패스워드가 일치하지 않을경우 (passwd가 null이 아님)
		// 두패스워드가 일치하지 않을 경우 실패
		// 두패스워드가 일치할 경우 성공
		
		if(passwd == null || !passwordEncoder.matches(member.getPasswd(), passwd)) {
			model.addAttribute("msg", "로그인실패");
			return "fail_back";
				
			
		} else {
			
			session.setAttribute("sId", member.getId());
			return "redirect:/";
		}
		
		
	}
	
	
	@GetMapping(value="logout.me")
	public String logout(HttpSession session) {
		session.invalidate();
		
		return "redirect:/";
	}
	
	//"/MemberInfo.me" 서블릿 요청에 대해 회원 정보 조회 비즈니스 로직 수행
	// 세션 아이디를 가져와서 아이디를 활용 
	@GetMapping("/MemberInfo.me")
	public String info(HttpSession session, Model model) {
		String id = (String)session.getAttribute("sId");
		
		//세션 아이디가 존재하지 않으면 fail_back.jsp를 활용하여 잘못된 접근입니다."출력
		if(id == null) {
			model.addAttribute("msg", "잘못된 접근입니다.");
			return "fail_back";
		}
		
		//else 없어도됨
		
		//조회 결과 memberVO객체를 model 객체에 "member"속성명으로 저장
		MemberVO member = service.getMemberInfo(id);
		System.out.println(member.getEmail());
		//문자열 분리(split)을 통해 email1,email2 에 나누어 저장하는 작업 추가 
		member.setEmail1( member.getEmail().split("@")[0]);
		member.setEmail2( member.getEmail().split("@")[1]);
		model.addAttribute("member", member); 
		
		//member_info.jsp 페이지로 포워딩 
		
		return "member/member_info";
		
	}
	
	
	@PostMapping("/MemberUpdate.me")
	public String update(MemberVO member, @RequestParam String newPasswd, Model model, HttpSession session) {
		//세션 아이디가 존재하지 않으면 잘못된 접근입니다. 
		String id = (String)session.getAttribute("sId");
		
		if(id == null) {
			model.addAttribute("msg", "잘못된 접근입니다.");
			return "fail_back";
		}
		
		//입력 받은 기존 비밀번호(passwd) 와 세션 아이디와 일치하는 데이터베이스 내의 레코드 비밀번호 비교 수행
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String passwd = service.getPasswd(id);
				
		if(passwd == null || !passwordEncoder.matches(member.getPasswd(), passwd)) {
			model.addAttribute("msg", "수정 권한 없음!");
			return "fail_back";
		}
		
		System.out.println(newPasswd);
		//만약, newPasswd파라미터가 존재할 경우 해싱 수행하여 덮어쓰기
		if(!newPasswd.equals("") ) {
			newPasswd= passwordEncoder.encode(newPasswd);
		}
		
		
		//MemberService - updateMemberInfo() 메서드를 호출하여 회원정보 수정 작업 요청
		//=> 파라미터 : MemberVO객체, 새 패스워드(newPasswd)리턴타입 : int(updateCount)
		
		int updateCount = service.updateMemberInfo(member,newPasswd);
		
		//"target"속성으로 이동할 페이지(MemberUpdate.me 저장후 success.jsp페이지로 포워딩 
		if(updateCount > 0) {
			model.addAttribute("msg", "회원정보 수정 성공!");
			model.addAttribute("target", "MemberInfo.me");
			return "success";
					
		} else {
			model.addAttribute("msg", "회원정보 수정 실패!");
			return "fail_back";
			
		}
		
		
	}
	@GetMapping("/MemberQuitForm.me")
	public String quitForm() {
		return "member/member_quit_form";
	}
	//회원탈퇴를 위한 비즈니스 로직 수행
	//파라미터 : 패스워드 , httpsession, model
	
	
	@PostMapping(value = "/MemberQuitPro.me")
	public String quitPro(@RequestParam String passwd, HttpSession session, Model model) {
		
		String id = (String)session.getAttribute("sId");
		
		String dbPasswd = service.getPasswd(id);
		
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		
		if(passwordEncoder.matches(passwd, dbPasswd)) {
			int deleteCount = service.quitMember(id);
			
			if(deleteCount > 0) {
				session.invalidate();
				model.addAttribute("msg", "탈퇴가 완료되었습니다!");
				model.addAttribute("target", "./");
				return "success";
			} else {
				
				model.addAttribute("msg", "권한이 없습니다!");
				return "fail_back";
			}
			
		
		} else {
			model.addAttribute("msg","권한이 없습니다!");
			return "fail_back";
		}
		
	}
	
}















