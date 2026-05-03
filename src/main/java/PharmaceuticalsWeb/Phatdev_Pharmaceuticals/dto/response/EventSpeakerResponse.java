//src/main/java/PharmaceuticalsWeb/Phatdev_Pharmaceuticals/dto/response/EventSpeakerResponse.java
package PharmaceuticalsWeb.Phatdev_Pharmaceuticals.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * =========================================================================
 * DTO: PHẢN HỒI THÔNG TIN DIỄN GIẢ
 * =========================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSpeakerResponse {
    private Long id;
    private Long ctEventId;
    private String fullName;
    private String academicTitle;
    private String organization;
    private String avatarUrl;
    private String bio;
}