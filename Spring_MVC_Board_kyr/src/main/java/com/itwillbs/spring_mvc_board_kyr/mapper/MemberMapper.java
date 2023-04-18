package com.itwillbs.spring_mvc_board_kyr.mapper;

import org.apache.ibatis.annotations.Param;

import com.itwillbs.spring_mvc_board_kyr.vo.MemberVO;

public interface MemberMapper {

	int insertMember(MemberVO member);

	//패스워드 조회 
	String selectPasswd(String id);

	MemberVO selectMember(String id);

	MemberVO selectMemberInfo(String id);

//	int updateMemberInfo(MemberVO member, String newPasswd);
	int updateMemberInfo(@Param("member") MemberVO member, @Param("newPasswd") String newPasswd);

	int deleteMember(String id);
	

}
