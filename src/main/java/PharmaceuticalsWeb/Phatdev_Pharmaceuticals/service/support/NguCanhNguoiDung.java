package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.service.support;

public class NguCanhNguoiDung {

    private final Long userId;
    private final int capBacCaoNhat;

    public NguCanhNguoiDung(Long userId, int capBacCaoNhat) {
        this.userId = userId;
        this.capBacCaoNhat = capBacCaoNhat;
    }

    public Long layUserId() {
        return userId;
    }

    public int layCapBacCaoNhat() {
        return capBacCaoNhat;
    }
}
