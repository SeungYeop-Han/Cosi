package SeungYeop_Han.Cosi.domains;

public enum OrderState {
    FILLED,     //완전 체결
    UNFILLED    //완전 미체결
                //현재가 기반으로 체결 진행하므로 부분체결은 제외
}
