package com.itwillbs.spring_mvc_board_kyr.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
	
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.itwillbs.spring_mvc_board_kyr.service.BoardService;
import com.itwillbs.spring_mvc_board_kyr.vo.BoardVO;

@Controller
public class BoardController {
	
	@Autowired
	private BoardService service;
	
	//글쓰기 폼
	//=> 세션 아이디가 존재 하지 않으면"로그인 필수!" 저장후 fail_back으로 이동
	
	@GetMapping("/BoardWriteForm.bo")
	public String writeForm(HttpSession session, Model model)	{
		String id = (String)session.getAttribute("sId");
		if(id == null) {
			model.addAttribute("msg", "로그인 필수!");
			model.addAttribute("target", "MemberLoginForm.me");
			return "success";
			
		}
		
		return "board/board_write_form";
		
	}
	//파일 업로드기능이 포함됨(enctype="multipart/form-data") 폼 파라미터 처리할 경우
	// 1) 각 파라미터를 처리하면서 업로드 파일을 매핍 메서드에 multipartfile 타입으로 직접 처리하는 
	
//	@PostMapping("/BoardWritePro.bo")
//	public String writePro(String board_name, String board_subject, String board_content, MultipartFile file) {
//		System.out.println(board_name+","+board_subject+","+board_content);
//		System.out.println("업로드 파일명:" + file.getOriginalFilename());
//		
//		
//		
//		return"";
//	}
	
	// 2) 각 파일을 제외한 나머지 파라미터를 Map 타입으로 처리하고 , 파일은 MultipartFile 타입 변수로 처리 
	// => 주의! Map 타입 파라미터 선언시 @RequestParam 어노테이션 필수! 
	
	
//	@PostMapping("/BoardWritePro.bo")
//	public String writePro(@RequestParam Map<String, String> map, MultipartFile file) {
//		System.out.println(map.get("board_name")+" "+map.get("board_subject")+" "+map.get("board_content"));
//		System.out.println("업로드 파일명:" + file.getOriginalFilename());
//		
//		return"";
//	}
	
	//3) Multipart File 타입 멤버 변수를 포함하는 BoardVO 타입으로 모든 파라미터를 한꺼번에 처리
	// BaordVO 클래스에 MultipartFile 타입 멤버변수 선언시
	// 반드시 <input type="file">태그의 name 속성명과 동일한 이름의 멤버변수를 선언하고
	// Getter/Setter 정의필수!
	@PostMapping("/BoardWritePro.bo")
	public String writePro(BoardVO board, Model model, HttpSession session) {
//		System.out.println(map.get("board_name")+" "+map.get("board_subject")+" "+map.get("board_content"));
//		System.out.println("업로드 파일명:" + file.getOriginalFilename());
//		
		// 이클립스가 관리하는 프로젝트상의 업로드 폴더(upload)생성 필요 - resources 폴더에 생성(외부 접근용)
		// 이클립스가 관리하는 프로젝트 상의 가상 업로드  경로에 대한 실제 업로드 경로 알아내기
		// 세션 객체의getServletContext() 메서드를 호출하여 서블릿 컨텍스트 객체를 얻어낸후 
		// 다시  getRealPath() 메서드를 호출하여 실제 업로드 경로를 알아낼수 있다!
		// (JSP일 경우 request 객체로 getRealPath() 메서드를 호출이 가능함 
		String uploadDir = "/resources/upload";//프로젝트 상의 업로드경로
//		String saveDir = request.getServletContext().getRealPath(uploadDir);
		String saveDir = session.getServletContext().getRealPath(uploadDir);
		//D:\workspace_sts\Spring_MVC_Board\src\main\webapp\resources\ upload
		//D:\workspace_sts\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\Spring_MVC_Board\resources\ upload
//		System.out.println("실제 업로드 경로  :" + saveDir);
		
		try {
			//-----------------------------------------------------------------
			// 업로드 디렉토리를 날짜별 디렉토리로 분류하기 
			// => 하나의 디렉토리에 너무 많은 파일이 존재하면 로딩시간 길어짐
			// 따라서, 날짜별로 디렉토리를 구별하기 위해 java.util.Date 클래스 활용
			Date date = new Date();
			//SimpleDateFormat클래스를 활용하여 날짜형식을 변경
			//=> 단 편의상 디렉토리 구조를 그대로 나타내기 위해 -대신 /기호사용
			// 가장 정확히 표현 하려면 디렉토리 구분자를 File.seperator  상수로 사용)
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
//			String subDir = sdf.format(date);
			board.setBoard_file_path("/"+sdf.format(date));
			
			saveDir = saveDir + board.getBoard_file_path();
			
			//-----------------------------------------------------------------
			//java.nio.file.Paths 클래스의 get() 메서드를 호출하여
			// 실제 경로를 관리하는 java.nio.file.Path 타입 객체를 리턴받기(파라미터:
			Path path = Paths.get(saveDir);
			//Files 클래스의 createDirectories() 메서드를 호출하여  Path 객체가 관리하는 경로가 없으면 생성
			
			Files.createDirectories(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//BoardVO 객체에 전달도니 MultipartFile 객체 꺼내기
		// 단 복수개의 파일(파라미터) 관리되는 경우 MultipartFile[] 타입으로 꺼내기
		MultipartFile mFile = board.getFile();	//단일파일
//		MultipartFile[] mFiles = board.getFiles();//복수파일
		
		String originalFileName = mFile.getOriginalFilename();
		System.out.println("원본 파일명 : " + originalFileName);
		
		//파일명 중복 방지를 위한 대책 
		// 현재 시스템(서버)에서 랜덤ID 값을 추출하여 파일명 앞에 붙여
		// 랜덤 ID값_파일명.확장자 형식으로 중복 파일명 처리
		// => 랜덤ID생성은 java.util.UUID (UUID=범용고유식별자) 클래스 활용
		String uuid = UUID.randomUUID().toString();
//		System.out.println("UUID: " + uuid );
		// 생성된UUID 값을 원본 파일명 앞에 결함
		// 가장 먼저 만나는 _ 기호를 기준으로 문자열을 분리하여처리)
		//=> 단, 파일명 길이 조절을 위해UUID 중 맨 앞의 7자리 문자열만 활용 
		// =>  생성된 파일명을BaordVO객체의 board_file 변수에 저장
		
//		originalFileName = UUID.randomUUID().toString()  +"_"+originalFileName;
//		System.out.println("실제 업로드될 파일명 : " + originalFileName);
		board.setBoard_file(uuid.substring(0,8).toString()+"_"+originalFileName); //한줄로 파일처리 끝~!~!

		int insertCount = service.registBoard(board);
		if(insertCount>0) {
			try {
				mFile.transferTo(new File(saveDir,board.getBoard_file()));
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return "redirect:/BoardList.bo";
			
		}else {
			model.addAttribute("msg", "글쓰기 실패!");
			return "fail_back";
		}
		
	}
	
	//데이터 저장 model 가져올때 httpsession 
	
//	@PostMapping("/BoardWritePro.bo")
//	public String writePro(BoardVO board) {
//		System.out.println(board);
//		System.out.println("업로드 파일명:" + board.getFile().getOriginalFilename());
//		
//		
//		
//		return"";
//	}
	
}


















