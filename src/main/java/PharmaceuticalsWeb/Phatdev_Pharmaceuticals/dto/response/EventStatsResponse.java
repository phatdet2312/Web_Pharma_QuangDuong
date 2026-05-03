//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/EventStatsResponse.java

package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Thống kê tổng quan trang sự kiện.
 * Dùng cho Hero Stats trên events/list.html và admin/events.html.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventStatsResponse {

    /** Số buổi trong tháng hiện tại */
    private long eventsThisMonth;

    /** Tổng lượt đăng ký toàn hệ thống */
    private long totalRegistrations;

    /** Số loại sự kiện đang có */
    private long totalEventTypes;

    /** Tổng buổi sự kiện */
    private long totalSessions;

    /** (Admin) Tổng chiến dịch */
    private long totalCampaigns;

    /** (Admin) Số đã tham dự (status = ATTENDED) */
    private long totalAttended;

    /** (Admin) Số địa điểm đang có */
    private long totalLocations;

    /** (Admin) Số chiến dịch đang có ít nhất một buổi trong tương lai */
    private long activeCampaigns;

    /** (Admin) Số buổi có startTime > thời điểm hiện tại */
    private long upcomingSessions;

    /** (Admin) Số lượt đăng ký mới trong ngày hôm nay */
    private long todayRegistrations;

    /** (Admin) Tỉ lệ tham dự thực tế = (totalAttended / totalRegistrations) * 100 */
    private double attendanceRate;

    /** (Admin) Số địa điểm tổ chức trực tuyến (IS_ONLINE = 1) */
    private long onlineLocations;
}
