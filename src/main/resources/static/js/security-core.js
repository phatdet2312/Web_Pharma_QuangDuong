/**
 * =========================================================================
 * THƯ VIỆN BẢO MẬT: HTPHAT SECURITY CORE (CLIENT-SIDE)
 * Yêu cầu: Phải load thư viện CryptoJS trước file này.
 * BẢO VỆ TOÀN DIỆN MỌI MÔI TRƯỜNG: Hỗ trợ jQuery ($.ajax), Fetch API và Axios (XMLHttpRequest)!
 * =========================================================================
 */

// ĐỌC CẤU HÌNH TỪ WEB CHỦ (Mặc định mảng rỗng nếu Web chủ quên cấu hình)
window.HtPhatSecurityConfig = window.HtPhatSecurityConfig || {
    publicUrls: [],
    //Khai báo lãnh thổ tin cậy (Dành cho Microservices/Backend khác Domain)
    trustedDomains: [],
    //Lỗ cắm (Hook) để Web chủ tự định nghĩa cách hiển thị lỗi
    onSecurityError: null,
    //biến chứa câu thông báo (Để hờ, Web chủ có thể đè lên)
    storageErrorMessage: null
};

// Hàm kiểm tra xem URL hiện tại có nằm trong danh sách Public (không cần băm) không
function kiemTraUrlCongKhai(uri) {
    if (uri === undefined || uri === null) {
        return false;
    }

    const danhSachPublic = window.HtPhatSecurityConfig.publicUrls;
    for (let i = 0; i < danhSachPublic.length; i++) {
        //dùng startsWith để đảm bảo Path thực sự bắt đầu bằng URL công khai.
        if (uri.startsWith(danhSachPublic[i]) === true) {
            return true;
        }
    }

    return false;
}


//HÀM PHÂN BIỆT ĐỊCH TA (ZERO-COUPLING)
function kiemTraLaLanhThoNha(uri) {
    if (!uri) {
        return false;
    }

    try {
        // 1. Nếu Origin trùng khớp 100% với Web hiện tại -> Nhà mình!
        if (uri.origin === window.location.origin) {
            console.log(`[Security] url trùng web`);
            return true;
        }
        // 2. Nếu Origin nằm trong danh sách "Tin tưởng" do Web chủ khai báo -> Nhà mình!
        const lanhThoTinCay = window.HtPhatSecurityConfig.trustedDomains || [];
        for (let i = 0; i < lanhThoTinCay.length; i++) {
            const domain = lanhThoTinCay[i];
            //CHỐNG SPOOFING TÊN MIỀN
            // 1. Trùng khớp hoàn toàn host (VD: api.hutech.com:8080)
            // 2. Trùng khớp hoàn toàn hostname (Bỏ qua port)
            // 3. Là Subdomain cấp dưới hợp lệ (Phải bắt đầu bằng dấu chấm, VD: .hutech.com)
            if (uri.host === domain ||
                uri.hostname === domain ||
                uri.hostname.endsWith('.' + domain)) {

                return true;
            }
        }
    } catch (e) {
        console.log(`[Security] lỗi phân tích url`);
        return false;
    }

    // Gặp Cloudinary, AWS, Stripe... -> Ngoại bang!
    console.log(`[Security] url chuyển đến server bên thứ 3`);
    return false;
}


// =========================================================================
// 1. CÔNG CỤ TRÍCH XUẤT DNA FILE (Hỗ trợ cấu hình số KB đầu/cuối)
// =========================================================================
function readFileAsBase64(blob) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        // Đọc dạng Base64 để chống hỏng dữ liệu khi băm ảnh/video (Binary)
        reader.onload = () => resolve(reader.result.split(',')[1] || "");
        reader.onerror = reject;
        reader.readAsDataURL(blob);
    });
}

async function getFileDNA(file, kbASize = 1, kbBSize = 1) {
    if (!file || file.size === 0) {
        return "empty_file";
    }

    const bytesA = kbASize * 1024;
    const bytesB = kbBSize * 1024;
    let rawName = file.name;
    if (rawName === undefined || rawName === null || rawName === "") {
        rawName = "unknown_filename";
    }

    const name = String(rawName).normalize('NFC');
    const size = file.size;
    let type = file.type;
    if (type === undefined || type === null || type === "") {
        type = "application/octet-stream";
    }

    // Cắt 1KB đầu và 1KB cuối
    const headBlob = file.slice(0, Math.min(bytesA, size));
    const tailBlob = file.slice(Math.max(0, size - bytesB), size);

    // Chờ đọc xong bằng Base64
    const [headB64, tailB64] = await Promise.all([
        readFileAsBase64(headBlob),
        readFileAsBase64(tailBlob)
    ]);

    // CÔNG THỨC: Tên File | Dung lượng | Loại file | N bytes đầu | N bytes cuối
    const dnaString = String(name.length) + ":" + name + "|" +
        String(size).length + ":" + size + "|" +
        String(type.length) + ":" + type + "|" +
        String(headB64.length) + ":" + headB64 + "|" +
        String(tailB64.length) + ":" + tailB64 + "|";

    return CryptoJS.SHA256(dnaString).toString(CryptoJS.enc.Hex);
}

//biến toàn cục tính độ lệch giờ (Offset)
let timeOffset = 0;

// Hàm đo độ lệch giờ mỗi khi có phản hồi từ Server
function syncTime(serverDateHeader) {
    if (serverDateHeader) {
        const serverTime = new Date(serverDateHeader).getTime();
        const localTime = new Date().getTime();
        const offset = serverTime - localTime;
        // Chỉ chấp nhận độ lệch tối đa 1 giờ (3600000ms). Chống Hacker tiêm Date 2099!
        if (Math.abs(offset) < 3600000) {
            timeOffset = offset;
        }
    }
}

// =========================================================================
// KHỐI ĐỘC LẬP A: CỖ MÁY BĂM DỮ LIỆU ĐA NĂNG (DÙNG CHUNG)
// Tách nguyên vẹn logic của ngài ra đây để tái sử dụng 100% không lặp code!
// =========================================================================
async function coreSecurityHashBody(dataInput, contentTypeInput) {
    let precalculatedHash = "EMPTY_BODY";
    let newData = dataInput;
    let newContentType = contentTypeInput;

    // [BỔ SUNG] Phân nhánh xử lý FormData
    if (dataInput instanceof FormData) {
        console.log("[Security] Đang xử lý băm DNA cho dữ liệu Multipart...");

        let logicalBody = {};
        const keys = Array.from(new Set(dataInput.keys()));

        // Lặp qua tất cả dữ liệu trong Form
        for (let i = 0; i < keys.length; i++) {
            let key = keys[i];
            let value = dataInput.getAll(key);
            let textIndex = 0;
            let fileIndex = 0;

            for (let v = 0; v < value.length; v++) {
                let val = value[v];
                if (val instanceof Blob) {//File kế thừa từ Blob, nhưng một Blob thuần túy khi truyền qua Ajax vẫn được hệ thống coi là File nhị phân
                    let keyBam = "F:" + key + "[" + fileIndex + "]";
                    if (val.size > 0) {
                        // File có nội dung -> trích xuất DNA
                        logicalBody[keyBam] = await getFileDNA(val, 1, 1);
                    } else {
                        // File 0 byte -> Trả về empty_file
                        logicalBody[keyBam] = "empty_file";
                    }
                    fileIndex++;
                } else {
                    let keyBam = "T:" + key + "[" + textIndex + "]";
                    // Không phải file thì chuyển chuỗi bình thường
                    if (val === null || val === undefined) {
                        logicalBody[keyBam] = "";
                    } else {
                        logicalBody[keyBam] = val.toString();
                    }
                    textIndex++;
                }
            }
        }

        // Ép về chuỗi chuẩn, sắp xếp key theo Alphabet để chống Canonicalization Attack
        const sortedKeys = Object.keys(logicalBody).sort();
        let cleanBodyString = "";
        for (let j = 0; j < sortedKeys.length; j++) {
            let originalKey = sortedKeys[j]; // GIỮ KEY GỐC
            let rawValue = logicalBody[originalKey]; // LẤY VALUE BẰNG KEY GỐC
            let k = String(originalKey).normalize('NFC'); // Chuẩn hóa Unicode
            let v = String(rawValue).normalize('NFC');

            // THUẬT TOÁN ĐỈNH CAO: LENGTH-PREFIXED (Mã hóa Tiền tố Độ dài)
            // Không cần encodeURIComponent, không sợ ký tự đặc biệt!
            cleanBodyString += k.length + ":" + k + "|" + v.length + ":" + v + "|";
        }

        // Tính trước mã băm Body
        precalculatedHash = CryptoJS.SHA256(cleanBodyString).toString(CryptoJS.enc.Hex);

    }
    // [BỔ SUNG] DỜI TOÀN BỘ KHỐI XỬ LÝ JSON TỪ BEFORESEND LÊN ĐÂY
    // CÒN NẾU LÀ JSON BÌNH THƯỜNG
    else if (dataInput !== undefined && dataInput !== null) {
        let jsonString = "";

        //LẤY CONTENT-TYPE THỰC TẾ (JQUERY MẶC ĐỊNH LÀ FORM-URLENCODED)
        let actualContentType = contentTypeInput;
        if (actualContentType === undefined || actualContentType === null || actualContentType === false || actualContentType === "") {
            actualContentType = "application/x-www-form-urlencoded";
        }

        //Chuẩn hóa chữ thường để chống lỗi JSON Case-Sensitive
        let safeContentType = String(actualContentType).toLowerCase();
        // 1. Nếu Dev đã truyền sẵn Chuỗi (Có thể là JSON stringified hoặc chuỗi URL-Encoded)
        if (typeof dataInput === 'string') {
            if (safeContentType && safeContentType.indexOf('application/x-www-form-urlencoded') !== -1) {
                let logicalBody = {};
                const urlParams = new URLSearchParams(dataInput);
                // Chuyển Query String thành Object để đồng bộ với Java
                let indexTracker = {};
                const entries = Array.from(urlParams.entries());

                for (let i = 0; i < entries.length; i++) {
                    let k = entries[i][0];
                    let v = entries[i][1];
                    let cleanK = k.normalize('NFC');

                    // Nếu Key này chưa xuất hiện, khởi tạo bộ đếm = 0
                    if (indexTracker[cleanK] === undefined) {
                        indexTracker[cleanK] = 0;
                    }

                    logicalBody[cleanK + "[" + indexTracker[cleanK] + "]"] = v.normalize('NFC');
                    indexTracker[cleanK]++; // Tăng bộ đếm cho lần sau
                }
                const sortedKeys = Object.keys(logicalBody).sort();
                for (let j = 0; j < sortedKeys.length; j++) {
                    let originalKey = sortedKeys[j];
                    let rawValue = logicalBody[originalKey];
                    let k = String(originalKey).normalize('NFC');
                    let v = String(rawValue).normalize('NFC');
                    jsonString += k.length + ":" + k + "|" + v.length + ":" + v + "|";
                }
            } else {
                newData = dataInput.trim();
                //Chuỗi JSON thuần (Cho BodyHashCheckerAdvice)
                jsonString = newData;
            }

        } // 2. Nếu Dev truyền Object VÀ yêu cầu gửi JSON rành mạch
        else if (typeof dataInput === 'object' &&
            actualContentType &&
            actualContentType.indexOf('application/json') !== -1) {
            jsonString = JSON.stringify(dataInput);
            // Ép chuẩn lại để truyền đi đúng dạng
            newData = jsonString;
            newContentType = "application/json; charset=utf-8";
        }
        // 3. Nếu truyền Object nhưng gửi dạng URL-Encoded (mặc định của jQuery/Fetch thông thường)
        else if (typeof dataInput === 'object') {
            let logicalBody = {};

            //hàm đệ quy  để phẳng hóa (flatten) Mảng
            function flattenArray(key, value) {
                if (Array.isArray(value)) {
                    if (value.length === 0) {
                        //Không làm gì cả. Vứt bỏ hoàn toàn để đồng bộ với hành vi của $.param()
                    } else {
                        for (let v = 0; v < value.length; v = v + 1) {
                            let valStr;
                            if (value[v] === null || value[v] === undefined) {
                                valStr = "";
                            } else {
                                valStr = String(value[v]);
                            }
                            logicalBody[key + "[" + v + "]"] = valStr;
                        }
                    }
                } else {
                    let valStr;
                    if (value === null || value === undefined) {
                        valStr = "";
                    } else {
                        valStr = String(value);
                    }
                    logicalBody[key + "[0]"] = valStr; // Ép mọi thứ về chuẩn mảng 1 phần tử
                }
            }

            const dataKeys = Object.keys(dataInput);
            for (let j = 0; j < dataKeys.length; j++) {
                let originalKey = dataKeys[j];
                let rawValue = dataInput[originalKey];// LẤY VALUE GỐC
                if (typeof rawValue === 'object' && !Array.isArray(rawValue) && rawValue !== null) {
                    console.error("[Security] LỖI: Không hỗ trợ băm Object đa tầng.");
                    return { hash: "ERROR_NESTED_OBJECT", data: newData, contentType: newContentType };
                }
                flattenArray(originalKey, rawValue);// Xử lý giá trị hợp lệ
            }

            // Sắp xếp các key ĐÃ PHẲNG HÓA và nối chuỗi Length-Prefixed
            const sortedKeys = Object.keys(logicalBody).sort();
            for (let j = 0; j < sortedKeys.length; j++) {
                let originalKey = sortedKeys[j];
                let rawValue = logicalBody[originalKey];

                let k = String(originalKey).normalize('NFC');
                let v = String(rawValue).normalize('NFC');
                jsonString += k.length + ":" + k + "|" + v.length + ":" + v + "|";
            }

            // Serialize dữ liệu để gửi đi hỗ trợ đa nền tảng
            if (typeof jQuery !== 'undefined') {
                newData = jQuery.param(dataInput, true); //=trrue ngăn hành vi mặc định của $.param() thêm [] vào các key
            } else {
                const searchParams = new URLSearchParams();
                Object.keys(dataInput).forEach(k => {
                    const val = dataInput[k];
                    if (Array.isArray(val)) val.forEach(v => searchParams.append(k, v));
                    else searchParams.append(k, val);
                });
                newData = searchParams.toString();
            }
        }

        if (jsonString !== "") {
            precalculatedHash = CryptoJS.SHA256(jsonString).toString(CryptoJS.enc.Hex);
        }
    }

    return { hash: precalculatedHash, data: newData, contentType: newContentType };
}

// =========================================================================
// KHỐI ĐỘC LẬP B: TẠO CHỮ KÝ BẢO MẬT (DÙNG CHUNG)
// Logic nguyên bản từ $.ajaxSetup.beforeSend của ngài
// =========================================================================
function coreSecurityGenerateHeaders(method, urlObj, bodyHash, clientSecret) {
    const realTime = new Date().getTime() + timeOffset;
    const timestamp = realTime.toString();
    const fullUri = urlObj.pathname + urlObj.search;

    //LẤY TÊN MIỀN CỦA FRONTEND (Ví dụ: phatdet2312.id.vn)
    const targetDomain = urlObj.host;

    // THUẬT TOÁN TẠO CHỮ KÝ: METHOD | URI | TIMESTAMP | BODY_HASH
    //CHỐNG TẤN CÔNG DÍNH CHÙM (Canonicalization Attack)
    const rawData = method + "|" + targetDomain + "|" + fullUri + "|" + timestamp + "|" + bodyHash;
    const signature = CryptoJS.HmacSHA256(rawData, clientSecret).toString(CryptoJS.enc.Hex);

    return {
        "X-Timestamp": timestamp,
        "X-Signature": signature,
        "X-Body-Hash": bodyHash,
        "X-Target-Domain": targetDomain
    };
}

// =========================================================================
// KHỐI ĐỘC LẬP C: PHỤC HỒI CHÌA KHÓA & THỜI GIAN
// Logic nguyên bản từ $.ajaxSetup.complete của ngài
// =========================================================================
function coreSecurityComplete(getHeaderFunc) {
    // PHỤC HỒI CLIENT SECRET KHI TẮT TRÌNH DUYỆT //với GET dđủ yêu cầu
    const recoveredSecret = getHeaderFunc('X-Client-Secret');
    if (recoveredSecret && !sessionStorage.getItem('clientSecret')) {
        sessionStorage.setItem('clientSecret', recoveredSecret);
        console.log("[Security] Đã tự động phục hồi chìa khóa bị mất do tắt trình duyệt!");
    }
    // ĐỒNG BỘ GIỜ VỚI SERVER THÔNG QUA HEADER CHUẨN CỦA HTTP
    const serverDate = getHeaderFunc('Date');
    syncTime(serverDate);
}


// =========================================================================
// 2. MONKEY PATCHING: ĐÁNH TRÁO HÀM $.ajax CỦA JQUERY
// Kỹ thuật này giúp bạn không phải sửa bất kỳ dòng code gọi AJAX nào ở Controller!
// =========================================================================
if (typeof jQuery !== 'undefined') {
    const originalAjax = $.ajax;
    $.ajax = function (url, options) {
        // Chuẩn hóa tham số (vì $.ajax có thể gọi kiểu $.ajax(url, options) hoặc $.ajax(options))
        if (typeof url === "object") {
            options = url;
            url = undefined;
        }
        if (options === undefined || options === null) {
            options = {};
        }

        // =========================================================================
        // 🛡️BẮT CHUẨN ALIAS CỦA JQUERY (type vs method)
        // Đảm bảo tương thích 100% với các Dev dùng cú pháp method: 'POST'
        // =========================================================================
        let method = "GET";
        if (options.type !== undefined && options.type !== null) {
            method = options.type.toUpperCase();
        } else if (options.method !== undefined && options.method !== null) {
            method = String(options.method).toUpperCase(); // Jquery 1.9.0+ Support
        }

        // =========================================================================
        // TẠO URL OBJECT ĐÚNG 1 LẦN VÀ DÙNG CHUNG CHO TOÀN BỘ!
        // ÉP KIỂU TUYỆT ĐỐI TRÁNH TOCTOU OBJECT SPOOFING
        // Ép mọi giá trị url về chuỗi nguyên thủy (String) để triệt tiêu DOM Element rác
        // =========================================================================
        const rawUrl = String(url || options.url || "").trim();
        // Khóa chết tham số, tước đoạt quyền dùng Object ảo của JQuery
        url = rawUrl;
        options.url = rawUrl;
        let targetUrlObj;
        let targetPath;
        try {
            // Hàm này tự xử lý luôn vụ URL tương đối ('/api/...') thành tuyệt đối
            targetUrlObj = new URL(rawUrl, window.location.origin);
            targetPath = targetUrlObj.pathname;
        } catch (e) {
            // Nếu URL sai định dạng, cứ thả trôi cho JQuery gốc báo lỗi
            return originalAjax(url, options);
        }

        // 1. KIỂM TRA NGOẠI BANG NGAY TẠI CỬA (TRÁNH LÃNG PHÍ CPU BĂM FILE CHO BÊN THỨ 3)
        if (kiemTraLaLanhThoNha(targetUrlObj) === false) {
            options._isKhongLaNguoiNha = true;
            return originalAjax(url, options); // Trả thẳng về Jquery gốc, thoát ngay lập tức!
        }

        //2. KIỂM TRA PUBLIC API BẰNG OBJECT
        if (kiemTraUrlCongKhai(targetPath) === true) {
            options._isCongKhai = true;
            return originalAjax(url, options);
        }

        // =========================================================================
        // LÁ CHẮN VÒNG ĐỜI (LIFECYCLE INTERCEPTOR) - BẢO TỒN CẤU HÌNH CỦA DEV
        // Giữ lại hàm beforeSend và complete của Dev, tiêm hàm của Thư viện vào trước!
        // =========================================================================
        const devBeforeSend = options.beforeSend;
        const devComplete = options.complete;

        options.beforeSend = function (xhr, settings) {
            if (method === 'POST' || method === 'PUT' || method === 'DELETE' || method === 'PATCH') {
                const clientSecret = sessionStorage.getItem('clientSecret');
                if (clientSecret) {
                    // ĐỌC MÃ BĂM ĐÃ ĐƯỢC TÍNH TRƯỚC (NẾU LÀ FORMDATA/FILE)
                    let bHash = settings._precalculatedHash || "EMPTY_BODY";

                    const secHeaders = coreSecurityGenerateHeaders(method, targetUrlObj, bHash, clientSecret);

                    // Đóng dấu vào Header
                    xhr.setRequestHeader("X-Timestamp", secHeaders["X-Timestamp"]);
                    xhr.setRequestHeader("X-Signature", secHeaders["X-Signature"]);
                    xhr.setRequestHeader("X-Body-Hash", secHeaders["X-Body-Hash"]);
                    xhr.setRequestHeader("X-Target-Domain", secHeaders["X-Target-Domain"]);

                    console.log("[Security] Đã ký tên cho request: " + (targetUrlObj.pathname + targetUrlObj.search) + " (Domain: " + secHeaders["X-Target-Domain"] + ")");
                } else {
                    console.warn("[Security] Thiếu clientSecret, request bị server từ chối.");
                }
            }

            // Chạy tiếp Code của Developer (nếu Dev có định nghĩa)
            if (typeof devBeforeSend === 'function') {
                return devBeforeSend.apply(this, arguments);
            }
        };

        options.complete = function (xhr, status) {
            // Chạy cốt lõi phục hồi của Thư viện
            coreSecurityComplete(function (key) { return xhr.getResponseHeader(key); });

            // Chạy tiếp Code của Developer (nếu có)
            if (typeof devComplete === 'function') {
                return devComplete.apply(this, arguments);
            }
        };


        // NẾU LÀ POST/PUT VÀ CÓ FILE (FormData) -> Can thiệp bất đồng bộ
        // [BỔ SUNG BẢO MẬT] Mở rộng điều kiện bắt mọi request làm thay đổi dữ liệu để ép chuẩn trước khi jQuery xử lý
        if (method === 'POST' || method === 'PUT' || method === 'DELETE' || method === 'PATCH') {

            // Trả về một Promise giả lập hành vi của AJAX
            const dfd = $.Deferred();
            // Biến lưu trữ request thực tế để có thể hủy nó
            let requestThucTe = null;

            //CẦU CHÌ LƯỢNG TỬ (CLOSURE STATE)
            // Biến này nằm trong RAM cục bộ của riêng mỗi lần gọi Ajax. 
            // Đảm bảo tuyệt đối Auto-Retry chỉ kích hoạt 1 lần duy nhất, 
            // bất chấp SessionStorage có lưu được chìa khóa hay không!
            let daThuBanLai = false;

            //CỜ HỦY DIỆT GÓI TIN ZOMBIE
            let isAborted = false;

            //TẠO DUMMY XHR ĐỂ CHỐNG CRASH CHO CÁC PLUGIN BÊN THỨ 3
            const fakeXhr = {
                readyState: 0,
                status: 0,
                statusText: "pending",
                setRequestHeader: function (key, val) {
                    if (options.headers === undefined) options.headers = {};
                    options.headers[key] = val;
                    return this;
                },

                getResponseHeader: function (key) { return null; },
                getAllResponseHeaders: function () { return ""; },
                abort: function () {
                    console.warn("[Security] Hủy request an toàn từ giao diện.");
                    isAborted = true;
                    if (requestThucTe !== null) {
                        requestThucTe.abort();
                    }
                    dfd.rejectWith(this, [{ status: 0, statusText: "abort" }, "abort", "Request aborted"]);
                    return this;
                },
                always: function (cb) { dfd.always(cb); return this; }
            };

            (async () => {
                try {
                    // GỌI LÕI BĂM DỮ LIỆU ĐỘC LẬP
                    const hashResult = await coreSecurityHashBody(options.data, options.contentType);

                    if (hashResult.hash === "ERROR_NESTED_OBJECT") {
                        const fakeErrorXhr = { status: 0, responseJSON: { message: 'Lỗi dữ liệu không hợp lệ.' } };
                        dfd.rejectWith(this, [fakeErrorXhr, 'error', 'Không hỗ trợ Object đa tầng']);
                        return; // exit async IIFE
                    }

                    // Lưu mã Hash JSON vào options để chuyển xuống beforeSend đóng dấu
                    options._precalculatedHash = hashResult.hash;
                    options.data = hashResult.data;
                    if (hashResult.contentType !== undefined && hashResult.contentType !== null) {
                        options.contentType = hashResult.contentType;
                    }

                    // ĐIỂM CHỐT CHẶN TRƯỚC KHI BẮN:
                    // Nếu khách hàng đã ấn Hủy trong lúc ta đang băm file, thì RÚT LUI NGAY LẬP TỨC!
                    if (isAborted === true) {
                        console.warn("[Security] Khối Async phát hiện lệnh Hủy. Khử gói tin Zombie thành công!");
                        return;
                    }

                    // Trả luồng về cho AJAX gốc chạy (Lúc này nó sẽ đi qua lifecycle interceptor)
                    requestThucTe = originalAjax(url, options)
                        .done(function (data, textStatus, jqXHR) { dfd.resolveWith(this, [data, textStatus, jqXHR]); })
                        .fail(function (jqXHR, textStatus, errorThrown) {
                            // =================================================================
                            // TUYỆT KỸ BÓNG MA: TỰ ĐỘNG BẮN LẠI (AUTO-RETRY) 
                            // Bất chấp Server trả về 401, 403 hay 400, chỉ cần có Chìa khóa là nhặt!
                            // =================================================================
                            // CHỈ KÍCH HOẠT KHI: 
                            // 1. Server có gửi khóa về (recoveredSecret != null)
                            // 3. Cầu chì chưa đứt (daThuBanLai === false)
                            if (daThuBanLai === false) {

                                // KIỂM TRA TRẠNG THÁI: Trong túi đã có chìa khóa từ trước chưa?
                                const oldSecret = sessionStorage.getItem('clientSecret');
                                const recoveredSecret = jqXHR.getResponseHeader('X-Client-Secret');
                                // CHỈ KÍCH HOẠT BÓNG MA KHI: 
                                //Trước đó túi ta đang rỗng
                                if (recoveredSecret !== null && recoveredSecret !== "" && oldSecret === null) {

                                    //BẬT CẦU CHÌ LÊN: Khóa vĩnh viễn khả năng Retry lần nữa cho riêng gói tin này
                                    daThuBanLai = true;
                                    //CỜ XÁC NHẬN LƯU TRỮ THÀNH CÔNG
                                    let luuThanhCong = false;

                                    // 2. Thử lưu vào SessionStorage (Dùng Try-Catch để chống Browser Betrayal)
                                    try {
                                        sessionStorage.setItem('clientSecret', recoveredSecret);
                                        luuThanhCong = true;
                                    } catch (storageError) {
                                        console.warn("[Security] Cảnh báo: Trình duyệt từ chối lưu SessionStorage!", storageError);
                                        //Kích hoạt Lỗ cắm (Nếu Web chủ có cài đặt)
                                        // Thư viện chỉ đưa ra thông điệp, không can thiệp vào UI
                                        if (typeof window.HtPhatSecurityConfig.onSecurityError === 'function') {
                                            const thongDiep = window.HtPhatSecurityConfig.storageErrorMessage
                                                || "Lỗi hệ thống: Trình duyệt đang chặn bộ nhớ cục bộ. Vui lòng tắt chế độ Ẩn danh!";
                                            window.HtPhatSecurityConfig.onSecurityError(thongDiep, "STORAGE_ERROR");
                                        }
                                    }

                                    // CHỈ RETRY KHI TÚI ĐÃ THỰC SỰ CHỨA CHÌA KHÓA!
                                    if (luuThanhCong === true) {
                                        console.log("[Security] Bắt được khóa phục hồi! Tự động bắn lại gói tin (Auto-Retry 1 lần duy nhất)...");
                                        // 3. Bắn lại chính gói tin nguyên thủy đó (Không gắn cờ)
                                        originalAjax(url, options)
                                            .done(function (d, t, j) { dfd.resolveWith(this, [d, t, j]); })
                                            .fail(function (jx, t, e) { dfd.rejectWith(this, [jx, t, e]); });

                                        return; // 4. CHẶN ĐỨNG việc báo lỗi cho Frontend
                                    }
                                }
                            }

                            // NẾU CHẠY XUỐNG ĐƯỢC ĐÂY NGHĨA LÀ:
                            // 1. Lỗi 403, 500... 
                            // 2. Cầu chì đã đứt
                            // 3. Storage đầy cứng không thể lưu khóa
                            // -> Từ chối thẳng tay, kích hoạt báo lỗi trên giao diện, BẢO VỆ TÀI NGUYÊN!
                            dfd.rejectWith(this, [jqXHR, textStatus, errorThrown]);
                        });
                } catch (error) {
                    console.error("[Security] Lỗi tiền xử lý băm File:", error);
                    dfd.rejectWith(this, [null, "abort", error]);
                }

            })();
            // Trả về một Promise giả dạng jqXHR hoàn hảo
            return $.extend(dfd.promise(), fakeXhr);
        }

        // In ra Method và URL để dễ dàng theo dõi
        console.log(`[Security] Cho qua request (Method: ${method}) -> Đang tải: ${rawUrl}`);
        // Nếu request bình thường (GET, JSON...), chạy AJAX gốc luôn
        return originalAjax(url, options);
    };
}

// =========================================================================
// MONKEY PATCHING 3: ĐÁNH TRÁO HÀM FETCH CỦA TRÌNH DUYỆT (NATIVE FETCH API)
// Thiết kế 100% đối xứng với logic của $.ajax bên trên!
// =========================================================================
if (typeof window.fetch === 'function') {
    const originalFetch = window.fetch;
    window.fetch = async function (resource, init) {
        init = init || {};
        const rawUrl = (typeof resource === 'string') ? resource : (resource instanceof Request ? resource.url : String(resource));
        const method = String(init.method || (resource instanceof Request ? resource.method : 'GET')).toUpperCase();

        let targetUrlObj;
        try { targetUrlObj = new URL(rawUrl, window.location.origin); }
        catch (e) { return originalFetch(resource, init); }

        // 1. KIỂM TRA NGOẠI BANG NGAY TẠI CỬA 
        if (kiemTraLaLanhThoNha(targetUrlObj) === false || kiemTraUrlCongKhai(targetUrlObj.pathname) === true) {
            return originalFetch(resource, init);
        }

        if (method === 'POST' || method === 'PUT' || method === 'DELETE' || method === 'PATCH') {

            // CẦU CHÌ LƯỢNG TỬ
            let daThuBanLai = false;

            // KIỂM TRA LỆNH HỦY TỪ GIAO DIỆN TRƯỚC KHI BĂM (Tương đương CỜ HỦY DIỆT GÓI TIN ZOMBIE)
            if (init.signal && init.signal.aborted) {
                console.warn("[Security] Hủy request an toàn từ giao diện trước khi băm.");
                return Promise.reject(new DOMException('Aborted', 'AbortError'));
            }

            const clientSecret = sessionStorage.getItem('clientSecret');

            let bodyData = init.body;
            let contentType = null;
            if (init.headers) {
                const h = new Headers(init.headers);
                contentType = h.get('Content-Type');
            }

            try {
                // GỌI LÕI BĂM DỮ LIỆU ĐỘC LẬP
                const hashResult = await coreSecurityHashBody(bodyData, contentType);

                if (hashResult.hash === "ERROR_NESTED_OBJECT") {
                    return Promise.reject(new Error("Lỗi dữ liệu không hợp lệ: Không hỗ trợ Object đa tầng"));
                }

                // ĐIỂM CHỐT CHẶN TRƯỚC KHI BẮN:
                if (init.signal && init.signal.aborted) {
                    console.warn("[Security] Khối Async phát hiện lệnh Hủy. Khử gói tin Zombie thành công!");
                    return Promise.reject(new DOMException('Aborted', 'AbortError'));
                }

                init.body = hashResult.data;

                if (clientSecret) {
                    const secHeaders = coreSecurityGenerateHeaders(method, targetUrlObj, hashResult.hash, clientSecret);

                    if (!init.headers) init.headers = {};
                    const h = new Headers(init.headers);

                    // Đóng dấu vào Header
                    h.set("X-Timestamp", secHeaders["X-Timestamp"]);
                    h.set("X-Signature", secHeaders["X-Signature"]);
                    h.set("X-Body-Hash", secHeaders["X-Body-Hash"]);
                    h.set("X-Target-Domain", secHeaders["X-Target-Domain"]);

                    if (hashResult.contentType) h.set("Content-Type", hashResult.contentType);
                    if (init.credentials === undefined) init.credentials = 'include';

                    const finalHeaders = {};
                    h.forEach((value, key) => { finalHeaders[key] = value; });
                    init.headers = finalHeaders;

                    console.log("[Security] Đã ký tên cho request: " + (targetUrlObj.pathname + targetUrlObj.search) + " (Domain: " + secHeaders["X-Target-Domain"] + ")");
                } else {
                    console.warn("[Security] Thiếu clientSecret, request bị server từ chối.");
                }

                let response = await originalFetch(resource, init);

                // Chạy cốt lõi phục hồi của Thư viện
                coreSecurityComplete(function (key) { return response.headers.get(key); });

                // =================================================================
                // TUYỆT KỸ BÓNG MA: TỰ ĐỘNG BẮN LẠI (AUTO-RETRY) DÀNH CHO FETCH
                // =================================================================
                if (!response.ok && daThuBanLai === false) {
                    const oldSecret = sessionStorage.getItem('clientSecret');
                    const recoveredSecret = response.headers.get('X-Client-Secret');

                    // CHỈ KÍCH HOẠT BÓNG MA KHI: Trước đó túi ta đang rỗng
                    if (recoveredSecret !== null && recoveredSecret !== "" && oldSecret === null) {
                        daThuBanLai = true; // BẬT CẦU CHÌ LÊN
                        let luuThanhCong = false;

                        try {
                            sessionStorage.setItem('clientSecret', recoveredSecret);
                            luuThanhCong = true;
                        } catch (storageError) {
                            console.warn("[Security] Cảnh báo: Trình duyệt từ chối lưu SessionStorage!", storageError);
                            if (typeof window.HtPhatSecurityConfig.onSecurityError === 'function') {
                                const thongDiep = window.HtPhatSecurityConfig.storageErrorMessage
                                    || "Lỗi hệ thống: Trình duyệt đang chặn bộ nhớ cục bộ. Vui lòng tắt chế độ Ẩn danh!";
                                window.HtPhatSecurityConfig.onSecurityError(thongDiep, "STORAGE_ERROR");
                            }
                        }

                        // CHỈ RETRY KHI TÚI ĐÃ THỰC SỰ CHỨA CHÌA KHÓA!
                        if (luuThanhCong === true) {
                            console.log("[Security] Bắt được khóa phục hồi! Tự động bắn lại gói tin (Auto-Retry 1 lần duy nhất)...");

                            const newSecHeaders = coreSecurityGenerateHeaders(method, targetUrlObj, hashResult.hash, recoveredSecret);
                            const h2 = new Headers(init.headers);
                            h2.set("X-Timestamp", newSecHeaders["X-Timestamp"]);
                            h2.set("X-Signature", newSecHeaders["X-Signature"]);

                            const newFinalHeaders = {};
                            h2.forEach((value, key) => { newFinalHeaders[key] = value; });
                            init.headers = newFinalHeaders;

                            // 3. Bắn lại chính gói tin nguyên thủy đó
                            let retryResponse = await originalFetch(resource, init);
                            coreSecurityComplete(function (key) { return retryResponse.headers.get(key); });
                            return retryResponse;
                        }
                    }
                }

                // NẾU CHẠY XUỐNG ĐƯỢC ĐÂY NGHĨA LÀ THẤT BẠI THỰC SỰ
                return response;

            } catch (error) {
                console.error("[Security] Lỗi tiền xử lý băm File:", error);
                throw error;
            }
        }

        console.log(`[Security] Cho qua request (Method: ${method}) -> Đang tải: ${rawUrl}`);
        let response = await originalFetch(resource, init);
        coreSecurityComplete(function (key) { return response.headers.get(key); });
        return response;
    };
}

// =========================================================================
// MONKEY PATCHING 4: ĐÁNH TRÁO LÕI CỔ ĐẠI XMLHttpRequest (HỖ TRỢ AXIOS)
// Thiết kế bám sát nguyên lý DRY, dùng chung các Lõi Độc Lập!
// =========================================================================
if (typeof window.XMLHttpRequest !== 'undefined') {
    const originalOpen = XMLHttpRequest.prototype.open;
    const originalSetRequestHeader = XMLHttpRequest.prototype.setRequestHeader;
    const originalSend = XMLHttpRequest.prototype.send;

    XMLHttpRequest.prototype.open = function (method, url, async, user, password) {
        this._htphatMethod = method;
        this._htphatUrl = url;
        this._htphatAsync = async !== false;
        this._htphatHeaders = {};
        return originalOpen.apply(this, arguments);
    };

    XMLHttpRequest.prototype.setRequestHeader = function (header, value) {
        if (this._htphatHeaders) this._htphatHeaders[header] = value;
        return originalSetRequestHeader.apply(this, arguments);
    };

    XMLHttpRequest.prototype.send = function (body) {
        const self = this;
        const method = String(self._htphatMethod || 'GET').toUpperCase();
        const rawUrl = String(self._htphatUrl || '').trim();

        let targetUrlObj;
        try { targetUrlObj = new URL(rawUrl, window.location.origin); }
        catch (e) { return originalSend.call(self, body); }

        // KIỂM TRA: Gói tin đã được $.ajax hoặc fetch đóng dấu trước đó chưa? (Chống nhân đôi Header)
        let daDuocKiemDuyetTruocDo = false;
        const danhSachHeaderHienCo = Object.keys(self._htphatHeaders);
        for (let i = 0; i < danhSachHeaderHienCo.length; i++) {
            if (danhSachHeaderHienCo[i].toLowerCase() === 'x-signature') {
                daDuocKiemDuyetTruocDo = true;
                break;
            }
        }

        // 1. KIỂM TRA LÁ CHẮN CƠ BẢN HOẶC ĐÃ ĐƯỢC XỬ LÝ
        if (kiemTraLaLanhThoNha(targetUrlObj) === false ||
            kiemTraUrlCongKhai(targetUrlObj.pathname) === true ||
            self._htphatAsync === false || 
            daDuocKiemDuyetTruocDo === true) {

            let daPhucHoiKhoa = false;
            self.addEventListener('readystatechange', function () {
                if (self.readyState === XMLHttpRequest.HEADERS_RECEIVED && !daPhucHoiKhoa) {
                    daPhucHoiKhoa = true;
                    coreSecurityComplete(function (key) { return self.getResponseHeader(key); });
                }
            });
            return originalSend.call(self, body);
        }

        if (method === 'POST' || method === 'PUT' || method === 'DELETE' || method === 'PATCH') {
            const clientSecret = sessionStorage.getItem('clientSecret');

            let contentType = null;
            const headerKeys = Object.keys(self._htphatHeaders);
            for (let i = 0; i < headerKeys.length; i++) {
                if (headerKeys[i].toLowerCase() === 'content-type') {
                    contentType = self._htphatHeaders[headerKeys[i]];
                    break;
                }
            }

            // CẦU CHÌ HỦY ZOMBIE TỪ XHR.ABORT() (Tương đương AbortController)
            let isAborted = false;
            const originalAbort = self.abort;
            self.abort = function () {
                console.warn("[Security] Phát hiện lệnh Hủy từ Client (XHR).");
                isAborted = true;
                return originalAbort.apply(self, arguments);
            };

            coreSecurityHashBody(body, contentType).then(function (hashResult) {
                if (hashResult.hash === "ERROR_NESTED_OBJECT") {
                    console.error("[Security] Không hỗ trợ Object đa tầng");
                    self.abort(); return;
                }

                // ĐIỂM CHỐT CHẶN TRƯỚC KHI BẮN:
                if (isAborted) {
                    console.warn("[Security] Khử gói tin Zombie thành công (XHR)!");
                    return;
                }

                if (clientSecret) {
                    const secHeaders = coreSecurityGenerateHeaders(method, targetUrlObj, hashResult.hash, clientSecret);

                    originalSetRequestHeader.call(self, "X-Timestamp", secHeaders["X-Timestamp"]);
                    originalSetRequestHeader.call(self, "X-Signature", secHeaders["X-Signature"]);
                    originalSetRequestHeader.call(self, "X-Body-Hash", secHeaders["X-Body-Hash"]);
                    originalSetRequestHeader.call(self, "X-Target-Domain", secHeaders["X-Target-Domain"]);

                    if (hashResult.contentType && !contentType) {
                        originalSetRequestHeader.call(self, "Content-Type", hashResult.contentType);
                    }
                    console.log("[Security] Đã ký tên cho request: " + (targetUrlObj.pathname + targetUrlObj.search));
                } else {
                    console.warn("[Security] Thiếu clientSecret, request bị server từ chối.");
                }

                let daPhucHoiKhoa = false;
                self.addEventListener('readystatechange', function () {
                    if (self.readyState === XMLHttpRequest.HEADERS_RECEIVED && !daPhucHoiKhoa) {
                        daPhucHoiKhoa = true;
                        coreSecurityComplete(function (key) { return self.getResponseHeader(key); });
                    }
                });

                originalSend.call(self, hashResult.data);

            }).catch(function (err) {
                console.error("[Security] Lỗi tiền xử lý băm File:", err);
                self.abort();
            });

            return; // Ngắt luồng đồng bộ, ủy thác cho khối Async
        }

        console.log(`[Security] Cho qua request (Method: ${method}) -> Đang tải: ${rawUrl}`);
        let daPhucHoiKhoa = false;
        self.addEventListener('readystatechange', function () {
            if (self.readyState === XMLHttpRequest.HEADERS_RECEIVED && !daPhucHoiKhoa) {
                daPhucHoiKhoa = true;
                coreSecurityComplete(function (key) { return self.getResponseHeader(key); });
            }
        });
        return originalSend.call(self, body);
    };
}