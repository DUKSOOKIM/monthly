package com.example.monthly.service.admin;

import com.example.monthly.dto.SellerDto;
import com.example.monthly.mapper.AdminMapper;
import com.example.monthly.vo.AdminChartVo;
import com.example.monthly.vo.ProductVo;
import com.example.monthly.vo.SearchVo;
import com.example.monthly.vo.SubsVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {
    private final AdminMapper adminMapper;


//    관리자 로그인
    /**
     * 판매자 로그인
     * @param adminId
     * @param adminPassword
     * @return
     * @throws IllegalArgumentException 존재하지 않는 회원 id, pw로 조회하는 경우
     */
@Transactional(readOnly = true)
public Long findAdminNumber(String adminId, String adminPassword){
    if(adminId==null||adminPassword==null){throw new IllegalArgumentException("아이디,패스워드 누락!");}
    return Optional.ofNullable(adminMapper.findAdminNumber(adminId,adminPassword))
            .orElseThrow(() -> {throw new IllegalArgumentException("존재하지 않는 판매자 회원");});
}



// 차트 판매자 조회
    /**
     * 차트 판매자 조회
     * @return 판매자 정보 리스트
     */
    @Transactional(readOnly = true)
    public List<AdminChartVo> getSellerApplication() {
        return adminMapper.sellerApplication();
    }


// 월간 전체 매출액
    /**
     * 차트 매출액 조회
     * @return 판매자 매출액 리스트
     */
    @Transactional(readOnly = true)
    public List<AdminChartVo> getPaymentCount() {
        return adminMapper.paymentCount();
    }



    //판매자 검색결과 조회 띄우기
    public List<SellerDto> selectSeller(SearchVo searchVo) {
        return adminMapper.selectSeller(searchVo);
    }
    //상품 검색결과 조회 띄우기
    public List<ProductVo> searchProduct(SearchVo searchVo) {
        return adminMapper.searchProduct(searchVo);
    }
    //판매자에서 브랜드페이지로 이동 시 구독자 조회
    public List<ProductVo> selectSubUser(Long sellerNumber) {
        return adminMapper.selectSubUser(sellerNumber);
    }
    //브랜드 페이지 브랜드 명 띄우기
    public List<ProductVo> brandName(Long sellerNumber){return adminMapper.brandName(sellerNumber); }
    //브랜드 페이지 모든 구독자 리스트 띄우기
     public List<SubsVo> productSubsUserList(SearchVo searchVo){return adminMapper.productSubsUserList(searchVo);}


    //판매자 상태 변경
    public void statusModify(SellerDto sellerDto){
        if (sellerDto == null) {
            throw new IllegalArgumentException("수정 정보 누락");
        }
        adminMapper.update(sellerDto);
    }
    //상품 상태 변경
    public void goodsStModify(ProductVo productVo){
        if (productVo == null) {
            throw new IllegalArgumentException("수정 정보 누락");
        }
        adminMapper.updateProduct(productVo);
    }
    //구독 상태 변경
    public void remove(Long subsNumber){
        adminMapper.deleteSubs(subsNumber);
    }

    // 판매자 신청 현황 날짜 별 처리 현황
    @Transactional(readOnly = true)
    public List<AdminChartVo> getSellerStatusByDate() {
        return adminMapper.getSellerStatusByDate();
    }
    @Transactional(readOnly = true)
    public List<AdminChartVo> sellerMonth() {
        return adminMapper.sellerMonth();
    }
    @Transactional(readOnly = true)
    public List<AdminChartVo> threeAverage() {
        return adminMapper.threeAverage();
    }


}
