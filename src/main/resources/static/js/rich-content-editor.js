// static/js/rich-content-editor.js
// Rich Content Editor — Bộ công cụ soạn thảo nội dung HTML chuyên nghiệp
// Dùng chung cho mọi trang admin cần soạn thảo nội dung (posts, events, ...)
// Để chuyển editor sang element khác: thay đổi rceHienTaiEditorId và rceHienTaiPreviewId

// Biến ID của editor area và preview area hiện đang active
var rceHienTaiEditorId = 'rceEditorArea';
var rceHienTaiPreviewId = 'rcePreviewContent';

// Biến lưu vị trí con trỏ trước khi mở dialog
var viTriConTroDaLuu = null;

// Biến lưu loại lệnh màu đang chọn (foreColor hoặc hiliteColor)
var rceMauDangChon = 'foreColor';

// Biến trạng thái chế độ HTML source
var dangXemHtmlSource = false;

// Undo/Redo stack — lưu trạng thái HTML của editor
var editorUndoStack = [];
var editorRedoStack = [];
var editorUndoMax = 50;
var editorDebounceTimer = null;

// =========================================================================
// TOOLBAR — khởi tạo và render toolbar soạn thảo
// =========================================================================

// Khởi tạo toolbar soạn thảo nội dung — render các nút công cụ vào #rceToolbar
function khoiTaoEditorToolbar() {
    var toolbar = document.getElementById('rceToolbar');
    if (toolbar === null) return;

    var html = '';

    // Dropdown chọn kiểu khối (Block format)
    html += '<select class="rce-toolbar-select" title="Kiểu khối" onchange="thucThiLenhEditor(this.value);this.selectedIndex=0;">';
    html += '<option value="" disabled selected>Kiểu khối</option>';
    html += '<option value="p">Đoạn văn</option>';
    html += '<option value="h1">Tiêu đề H1</option>';
    html += '<option value="h2">Tiêu đề H2</option>';
    html += '<option value="h3">Tiêu đề H3</option>';
    html += '<option value="h4">Tiêu đề H4</option>';
    html += '<option value="blockquote">Trích dẫn</option>';
    html += '<option value="codeBlock">Khối code</option>';
    html += '</select>';

    // Dropdown chọn phông chữ
    html += '<select class="rce-toolbar-select" title="Phông chữ" onchange="doiPhongChu(this.value);this.selectedIndex=0;">';
    html += '<option value="" disabled selected>Phông chữ</option>';
    html += '<option value="inherit" style="font-family:inherit;">Mặc định</option>';
    html += '<option value="Arial, sans-serif" style="font-family:Arial;">Arial</option>';
    html += '<option value="Georgia, serif" style="font-family:Georgia;">Georgia</option>';
    html += '<option value="Times New Roman, serif" style="font-family:Times New Roman;">Times New Roman</option>';
    html += '<option value="Courier New, monospace" style="font-family:Courier New;">Courier New</option>';
    html += '<option value="Verdana, sans-serif" style="font-family:Verdana;">Verdana</option>';
    html += '<option value="Tahoma, sans-serif" style="font-family:Tahoma;">Tahoma</option>';
    html += '<option value="Trebuchet MS, sans-serif" style="font-family:Trebuchet MS;">Trebuchet MS</option>';
    html += '</select>';

    // Dropdown chọn kích thước chữ
    html += '<select class="rce-toolbar-select" title="Cỡ chữ" onchange="doiCoChu(this.value);this.selectedIndex=0;">';
    html += '<option value="" disabled selected>Cỡ chữ</option>';
    html += '<option value="10px">10px</option>';
    html += '<option value="12px">12px</option>';
    html += '<option value="14px">14px</option>';
    html += '<option value="16px">16px</option>';
    html += '<option value="18px">18px</option>';
    html += '<option value="20px">20px</option>';
    html += '<option value="24px">24px</option>';
    html += '<option value="28px">28px</option>';
    html += '<option value="32px">32px</option>';
    html += '<option value="36px">36px</option>';
    html += '<option value="48px">48px</option>';
    html += '</select>';

    // Dropdown line-height
    html += '<select class="rce-toolbar-select rce-toolbar-select-sm" title="Khoảng cách dòng" onchange="doiKhoangCachDong(this.value);this.selectedIndex=0;">';
    html += '<option value="" disabled selected>Dòng</option>';
    html += '<option value="1">1.0</option>';
    html += '<option value="1.25">1.25</option>';
    html += '<option value="1.5">1.5</option>';
    html += '<option value="1.75">1.75</option>';
    html += '<option value="2">2.0</option>';
    html += '<option value="2.5">2.5</option>';
    html += '</select>';

    html += '<div class="rce-toolbar-sep"></div>';

    // Nhóm nút định dạng và chức năng
    var cac_nut = [
        { nhom: 'format', items: [
            { cmd: 'bold', icon: 'fas fa-bold', title: 'In đậm (Ctrl+B)' },
            { cmd: 'italic', icon: 'fas fa-italic', title: 'In nghiêng (Ctrl+I)' },
            { cmd: 'underline', icon: 'fas fa-underline', title: 'Gạch chân (Ctrl+U)' },
            { cmd: 'strikeThrough', icon: 'fas fa-strikethrough', title: 'Gạch ngang' },
            { cmd: 'subscript', icon: 'fas fa-subscript', title: 'Chỉ số dưới (H₂O)' },
            { cmd: 'superscript', icon: 'fas fa-superscript', title: 'Chỉ số trên (m²)' }
        ]},
        { nhom: 'color', items: [
            { cmd: 'textColor', icon: 'fas fa-palette', title: 'Màu chữ' },
            { cmd: 'highlight', icon: 'fas fa-highlighter', title: 'Tô nền chữ' }
        ]},
        { nhom: 'align', items: [
            { cmd: 'justifyLeft', icon: 'fas fa-align-left', title: 'Căn trái' },
            { cmd: 'justifyCenter', icon: 'fas fa-align-center', title: 'Căn giữa' },
            { cmd: 'justifyRight', icon: 'fas fa-align-right', title: 'Căn phải' },
            { cmd: 'justifyFull', icon: 'fas fa-align-justify', title: 'Căn đều' }
        ]},
        { nhom: 'list', items: [
            { cmd: 'insertUnorderedList', icon: 'fas fa-list-ul', title: 'Danh sách gạch đầu dòng' },
            { cmd: 'insertOrderedList', icon: 'fas fa-list-ol', title: 'Danh sách đánh số' },
            { cmd: 'indent', icon: 'fas fa-indent', title: 'Thụt lề vào' },
            { cmd: 'outdent', icon: 'fas fa-outdent', title: 'Thụt lề ra' }
        ]},
        { nhom: 'insert', items: [
            { cmd: 'link', icon: 'fas fa-link', title: 'Chèn liên kết' },
            { cmd: 'image', icon: 'fas fa-image', title: 'Chèn hình ảnh' },
            { cmd: 'table', icon: 'fas fa-table', title: 'Chèn bảng' },
            { cmd: 'video', icon: 'fas fa-video', title: 'Chèn video YouTube' },
            { cmd: 'insertHorizontalRule', icon: 'fas fa-minus', title: 'Đường kẻ ngang' },
            { cmd: 'specialChar', icon: 'fas fa-flask', title: 'Ký tự đặc biệt y khoa' },
            { cmd: 'alertBox', icon: 'fas fa-exclamation-triangle', title: 'Hộp cảnh báo y khoa' },
            { cmd: 'emoji', icon: 'fas fa-smile', title: 'Biểu tượng cảm xúc' }
        ]},
        { nhom: 'action', items: [
            { cmd: 'removeFormat', icon: 'fas fa-eraser', title: 'Xóa định dạng' },
            { cmd: 'selectAll', icon: 'fas fa-object-group', title: 'Chọn tất cả' },
            { cmd: 'findReplace', icon: 'fas fa-search', title: 'Tìm & Thay thế' },
            { cmd: 'toggleHtml', icon: 'fas fa-file-code', title: 'Xem mã HTML' },
            { cmd: 'printContent', icon: 'fas fa-print', title: 'In nội dung' },
            { cmd: 'undo', icon: 'fas fa-undo', title: 'Hoàn tác (Ctrl+Z)' },
            { cmd: 'redo', icon: 'fas fa-redo', title: 'Làm lại (Ctrl+Y)' }
        ]}
    ];

    for (var g = 0; g < cac_nut.length; g = g + 1) {
        html += '<div class="rce-toolbar-sep"></div>';
        html += '<div class="rce-toolbar-group">';
        var items = cac_nut[g].items;
        for (var i = 0; i < items.length; i = i + 1) {
            var item = items[i];
            var labelHtml = '';
            if (item.label !== undefined) {
                labelHtml = '<sup style="font-size:.55rem;font-weight:800;margin-left:-2px;">' + item.label + '</sup>';
            }
            html += '<button type="button" class="rce-toolbar-btn" title="' + item.title + '" data-cmd="' + item.cmd + '" onclick="thucThiLenhEditor(\'' + item.cmd + '\')">';
            html += '<i class="' + item.icon + '"></i>' + labelHtml;
            html += '</button>';
        }
        html += '</div>';
    }

    // Thanh trạng thái: đếm từ + ký tự
    html += '<div class="rce-toolbar-sep"></div>';
    html += '<div class="rce-word-count" id="rceWordCount" title="Số từ / ký tự">0 từ | 0 ký tự</div>';

    toolbar.innerHTML = html;
}

// =========================================================================
// LỆNH EDITOR — thực thi lệnh khi nhấn nút toolbar
// =========================================================================

// Thực thi lệnh soạn thảo tương ứng với nút toolbar được nhấn
function thucThiLenhEditor(lenh) {
    var editor = document.getElementById(rceHienTaiEditorId);

    // Các lệnh mở dialog không cần focus trước
    if (lenh === 'link' || lenh === 'image' || lenh === 'table' || lenh === 'video'
        || lenh === 'textColor' || lenh === 'highlight' || lenh === 'specialChar' || lenh === 'alertBox') {
        // xử lý bên dưới
    } else if (lenh === 'toggleHtml') {
        // xử lý bên dưới
    } else {
        editor.focus();
    }

    // Heading & Paragraph
    if (lenh === 'h1' || lenh === 'h2' || lenh === 'h3' || lenh === 'h4' || lenh === 'p') {
        document.execCommand('formatBlock', false, '<' + lenh + '>');

    // Blockquote toggle
    } else if (lenh === 'blockquote') {
        editor.focus();
        var sel = window.getSelection();
        if (sel.rangeCount > 0) {
            var node = sel.anchorNode;
            while (node !== null && node !== editor) {
                if (node.nodeName === 'BLOCKQUOTE') {
                    document.execCommand('formatBlock', false, '<p>');
                    capNhatPreview();
                    luuTrangThaiEditor();
                    return;
                }
                node = node.parentNode;
            }
        }
        document.execCommand('formatBlock', false, '<blockquote>');

    // Code block
    } else if (lenh === 'codeBlock') {
        var selectedText = '';
        var sel2 = window.getSelection();
        if (sel2.rangeCount > 0) {
            selectedText = sel2.toString();
        }
        if (selectedText === '') selectedText = 'code here...';
        var codeHtml = '<pre><code>' + escapeHtml(selectedText) + '</code></pre><p><br></p>';
        document.execCommand('insertHTML', false, codeHtml);

    // Link dialog
    } else if (lenh === 'link') {
        luuViTriConTro();
        document.getElementById('rceDialogLink').style.display = 'flex';
        document.getElementById('rceLinkUrl').value = '';
        document.getElementById('rceLinkText').value = '';
        var sel3 = window.getSelection();
        if (sel3.rangeCount > 0 && sel3.toString().length > 0) {
            document.getElementById('rceLinkText').value = sel3.toString();
        }
        document.getElementById('rceLinkUrl').focus();
        return;

    // Image dialog
    } else if (lenh === 'image') {
        luuViTriConTro();
        document.getElementById('rceDialogImage').style.display = 'flex';
        document.getElementById('rceImageUrl').value = '';
        document.getElementById('rceImageAlt').value = '';
        document.getElementById('rceImageFile').value = '';
        document.getElementById('rceImageUrl').focus();
        return;

    // Table dialog
    } else if (lenh === 'table') {
        luuViTriConTro();
        moDialogBang();
        return;

    // Video YouTube dialog
    } else if (lenh === 'video') {
        luuViTriConTro();
        document.getElementById('rceDialogVideo').style.display = 'flex';
        document.getElementById('rceVideoUrl').value = '';
        document.getElementById('rceVideoUrl').focus();
        return;

    // Text color picker
    } else if (lenh === 'textColor') {
        luuViTriConTro();
        moDialogMau('foreColor');
        return;

    // Highlight color picker
    } else if (lenh === 'highlight') {
        luuViTriConTro();
        moDialogMau('hiliteColor');
        return;

    // Special characters
    } else if (lenh === 'specialChar') {
        luuViTriConTro();
        document.getElementById('rceDialogSpecialChar').style.display = 'flex';
        return;

    // Alert box (y khoa)
    } else if (lenh === 'alertBox') {
        luuViTriConTro();
        document.getElementById('rceDialogAlert').style.display = 'flex';
        document.getElementById('rceAlertContent').value = '';
        return;

    // Emoji picker
    } else if (lenh === 'emoji') {
        luuViTriConTro();
        document.getElementById('rceDialogEmoji').style.display = 'flex';
        return;

    // Find & Replace
    } else if (lenh === 'findReplace') {
        document.getElementById('rceDialogFindReplace').style.display = 'flex';
        document.getElementById('rceFindText').value = '';
        document.getElementById('rceReplaceText').value = '';
        document.getElementById('rceFindCount').textContent = '';
        document.getElementById('rceFindText').focus();
        return;

    // Select All
    } else if (lenh === 'selectAll') {
        editor.focus();
        document.execCommand('selectAll', false, null);
        return;

    // Print
    } else if (lenh === 'printContent') {
        inNoiDung();
        return;

    // Toggle HTML source
    } else if (lenh === 'toggleHtml') {
        batTatCheDoChuoiHtml();
        return;

    // Undo/Redo
    } else if (lenh === 'undo') {
        hoanTacEditor();
        return;
    } else if (lenh === 'redo') {
        lamLaiEditor();
        return;

    // Tất cả lệnh execCommand chuẩn (bold, italic, underline, justify, indent, etc.)
    } else {
        document.execCommand(lenh, false, null);
    }

    capNhatPreview();
    luuTrangThaiEditor();
}

// =========================================================================
// CON TRỎ — lưu và khôi phục vị trí con trỏ khi mở dialog
// =========================================================================

// Lưu vị trí con trỏ hiện tại trong editor
function luuViTriConTro() {
    var sel = window.getSelection();
    if (sel.rangeCount > 0) {
        viTriConTroDaLuu = sel.getRangeAt(0).cloneRange();
    }
}

// Khôi phục vị trí con trỏ đã lưu
function khoiPhucViTriConTro() {
    if (viTriConTroDaLuu !== null) {
        var sel = window.getSelection();
        sel.removeAllRanges();
        sel.addRange(viTriConTroDaLuu);
    }
}

// =========================================================================
// LINK — chèn liên kết
// =========================================================================

// Chèn liên kết vào editor từ dialog
function chenLienKet() {
    var url = document.getElementById('rceLinkUrl').value.trim();
    var text = document.getElementById('rceLinkText').value.trim();
    if (url === '') { showToast('warning', 'Vui lòng nhập URL'); return; }
    if (text === '') text = url;
    dongDialogLink();
    var editor = document.getElementById(rceHienTaiEditorId);
    editor.focus();
    khoiPhucViTriConTro();
    var linkHtml = '<a href="' + escapeHtml(url) + '" target="_blank">' + escapeHtml(text) + '</a>';
    document.execCommand('insertHTML', false, linkHtml);
    capNhatPreview();
    luuTrangThaiEditor();
}

// Đóng dialog chèn link
function dongDialogLink() {
    document.getElementById('rceDialogLink').style.display = 'none';
}

// =========================================================================
// IMAGE — chèn hình ảnh từ URL hoặc upload
// =========================================================================

// Chèn ảnh từ URL vào editor
function chenAnhTuUrl() {
    var url = document.getElementById('rceImageUrl').value.trim();
    var alt = document.getElementById('rceImageAlt').value.trim();
    if (url === '') { showToast('warning', 'Vui lòng nhập URL hình ảnh'); return; }
    dongDialogImage();
    var editor = document.getElementById(rceHienTaiEditorId);
    editor.focus();
    khoiPhucViTriConTro();
    var imgHtml = '<img src="' + escapeHtml(url) + '" alt="' + escapeHtml(alt) + '"><br>';
    document.execCommand('insertHTML', false, imgHtml);
    capNhatPreview();
    luuTrangThaiEditor();
}

// Upload ảnh từ file rồi chèn vào editor — reuse pattern $.ajax với JWT
function uploadAnhChoEditor() {
    var input = document.getElementById('rceImageFile');
    if (input === null || input.files === undefined || input.files.length === 0) {
        showToast('warning', 'Vui lòng chọn file ảnh.');
        return;
    }
    var file = input.files[0];
    if (file.type.indexOf('image/') !== 0) {
        showToast('warning', 'Chỉ được tải lên file ảnh (jpg, png, gif, webp).');
        return;
    }
    if (file.size > 5 * 1024 * 1024) {
        showToast('warning', 'Dung lượng file vượt quá 5MB.');
        return;
    }
    var formData = new FormData();
    formData.append('file', file);
    var jwtToken = localStorage.getItem('jwtToken');
    $.ajax({
        url: '/api/admin/posts/upload-thumbnail',
        method: 'POST',
        headers: { Authorization: 'Bearer ' + jwtToken },
        data: formData,
        contentType: false,
        processData: false,
        success: function(response) {
            if (response.status === 200 && response.data !== null && response.data !== undefined) {
                var uploadedUrl = response.data.url;
                document.getElementById('rceImageUrl').value = uploadedUrl;
                showToast('success', 'Tải ảnh thành công — nhấn "Chèn từ URL" để đưa vào nội dung');
            } else {
                showToast('error', response.message || 'Upload thất bại');
            }
        },
        error: function(xhr) {
            var msg = 'Lỗi tải ảnh';
            if (xhr.responseJSON && xhr.responseJSON.message) msg = xhr.responseJSON.message;
            showToast('error', msg);
        }
    });
}

// Đóng dialog chèn ảnh
function dongDialogImage() {
    document.getElementById('rceDialogImage').style.display = 'none';
}

// =========================================================================
// TABLE — chèn bảng qua grid picker
// =========================================================================

// Mở dialog chèn bảng — tạo grid picker 6x6
function moDialogBang() {
    var grid = document.getElementById('rceTableGrid');
    var html = '';
    for (var r = 0; r < 6; r = r + 1) {
        for (var c = 0; c < 6; c = c + 1) {
            html += '<div class="rce-table-cell" data-row="' + (r + 1) + '" data-col="' + (c + 1) + '" ';
            html += 'onmouseover="highlightTableCells(' + (r + 1) + ',' + (c + 1) + ')" ';
            html += 'onclick="chenBang(' + (r + 1) + ',' + (c + 1) + ')"></div>';
        }
    }
    grid.innerHTML = html;
    document.getElementById('rceTableSizeLabel').textContent = '0 × 0';
    document.getElementById('rceDialogTable').style.display = 'flex';
}

// Highlight cells trong table picker khi hover
window.highlightTableCells = function(row, col) {
    var cells = document.getElementById('rceTableGrid').children;
    for (var i = 0; i < cells.length; i = i + 1) {
        var r = parseInt(cells[i].getAttribute('data-row'));
        var c = parseInt(cells[i].getAttribute('data-col'));
        if (r <= row && c <= col) {
            cells[i].classList.add('highlight');
        } else {
            cells[i].classList.remove('highlight');
        }
    }
    document.getElementById('rceTableSizeLabel').textContent = row + ' × ' + col;
};

// Chèn bảng với kích thước đã chọn vào editor
window.chenBang = function(rows, cols) {
    dongDialogTable();
    var editor = document.getElementById(rceHienTaiEditorId);
    editor.focus();
    khoiPhucViTriConTro();
    var html = taoHtmlBang(rows, cols);
    document.execCommand('insertHTML', false, html);
    capNhatPreview();
    luuTrangThaiEditor();
};

// Tạo HTML bảng với số hàng và cột cho trước
function taoHtmlBang(rows, cols) {
    var html = '<table><thead><tr>';
    for (var c = 0; c < cols; c = c + 1) {
        html += '<th>Cột ' + (c + 1) + '</th>';
    }
    html += '</tr></thead><tbody>';
    for (var r = 0; r < rows; r = r + 1) {
        html += '<tr>';
        for (var c2 = 0; c2 < cols; c2 = c2 + 1) {
            html += '<td>&nbsp;</td>';
        }
        html += '</tr>';
    }
    html += '</tbody></table><p><br></p>';
    return html;
}

// Đóng dialog chèn bảng
function dongDialogTable() {
    document.getElementById('rceDialogTable').style.display = 'none';
}

// =========================================================================
// FONT — đổi phông chữ, cỡ chữ, khoảng cách dòng
// =========================================================================

// Đổi phông chữ cho đoạn text đang chọn
function doiPhongChu(fontFamily) {
    var editor = document.getElementById(rceHienTaiEditorId);
    editor.focus();
    if (fontFamily === 'inherit') {
        document.execCommand('removeFormat', false, null);
    } else {
        document.execCommand('fontName', false, fontFamily);
    }
    capNhatPreview();
    luuTrangThaiEditor();
}

// Đổi cỡ chữ cho đoạn text đang chọn bằng span + inline style
function doiCoChu(size) {
    var editor = document.getElementById(rceHienTaiEditorId);
    editor.focus();
    var sel = window.getSelection();
    if (sel.rangeCount === 0 || sel.isCollapsed === true) {
        showToast('warning', 'Vui lòng bôi đen đoạn text cần đổi cỡ chữ');
        return;
    }
    // Dùng fontSize execCommand với giá trị tạm, rồi thay font tag bằng span
    document.execCommand('fontSize', false, '7');
    var fontTags = editor.querySelectorAll('font[size="7"]');
    for (var i = 0; i < fontTags.length; i = i + 1) {
        var span = document.createElement('span');
        span.style.fontSize = size;
        span.innerHTML = fontTags[i].innerHTML;
        fontTags[i].parentNode.replaceChild(span, fontTags[i]);
    }
    capNhatPreview();
    luuTrangThaiEditor();
}

// Đổi khoảng cách dòng cho đoạn hiện tại
function doiKhoangCachDong(lineHeight) {
    var editor = document.getElementById(rceHienTaiEditorId);
    editor.focus();
    var sel = window.getSelection();
    if (sel.rangeCount > 0) {
        var node = sel.anchorNode;
        // Tìm block element cha gần nhất
        while (node !== null && node !== editor) {
            if (node.nodeType === 1) {
                var tag = node.nodeName;
                if (tag === 'P' || tag === 'DIV' || tag === 'H1' || tag === 'H2' || tag === 'H3' || tag === 'H4' || tag === 'LI' || tag === 'BLOCKQUOTE') {
                    node.style.lineHeight = lineHeight;
                    break;
                }
            }
            node = node.parentNode;
        }
    }
    capNhatPreview();
    luuTrangThaiEditor();
}

// =========================================================================
// EMOJI PICKER — bảng biểu tượng cảm xúc
// =========================================================================

// Đóng dialog emoji
window.dongDialogEmoji = function() {
    document.getElementById('rceDialogEmoji').style.display = 'none';
};

// Chèn emoji vào editor
window.chenEmoji = function(emoji) {
    document.getElementById('rceDialogEmoji').style.display = 'none';
    var editor = document.getElementById(rceHienTaiEditorId);
    editor.focus();
    khoiPhucViTriConTro();
    document.execCommand('insertText', false, emoji);
    capNhatPreview();
    luuTrangThaiEditor();
};

// =========================================================================
// FIND & REPLACE — tìm và thay thế text trong editor
// =========================================================================

// Đóng dialog tìm thay thế
window.dongDialogFindReplace = function() {
    document.getElementById('rceDialogFindReplace').style.display = 'none';
    xoaHighlightTimKiem();
};

// Tìm kiếm text trong editor — highlight tất cả kết quả
window.timKiemTrongEditor = function() {
    var tuKhoa = document.getElementById('rceFindText').value;
    if (tuKhoa === '') return;
    xoaHighlightTimKiem();
    var editor = document.getElementById(rceHienTaiEditorId);
    var htmlGoc = editor.innerHTML;
    var regex = new RegExp('(' + tuKhoa.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + ')', 'gi');
    editor.innerHTML = htmlGoc.replace(regex, '<mark class="rce-find-hl">$1</mark>');
    var soKetQua = editor.querySelectorAll('.rce-find-hl').length;
    document.getElementById('rceFindCount').textContent = soKetQua + ' kết quả';
};

// Thay thế tất cả kết quả tìm được
window.thayTheTatCa = function() {
    var tuKhoa = document.getElementById('rceFindText').value;
    var thayBang = document.getElementById('rceReplaceText').value;
    if (tuKhoa === '') return;
    xoaHighlightTimKiem();
    var editor = document.getElementById(rceHienTaiEditorId);
    var htmlGoc = editor.innerHTML;
    var regex = new RegExp(tuKhoa.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'gi');
    editor.innerHTML = htmlGoc.replace(regex, thayBang);
    capNhatPreview();
    luuTrangThaiEditor();
    showToast('success', 'Đã thay thế tất cả');
    document.getElementById('rceFindCount').textContent = '0 kết quả';
};

// Xóa tất cả highlight tìm kiếm trong editor
function xoaHighlightTimKiem() {
    var editor = document.getElementById(rceHienTaiEditorId);
    var marks = editor.querySelectorAll('.rce-find-hl');
    for (var i = 0; i < marks.length; i = i + 1) {
        var parent = marks[i].parentNode;
        parent.replaceChild(document.createTextNode(marks[i].textContent), marks[i]);
        parent.normalize();
    }
}

// =========================================================================
// PRINT — in nội dung soạn thảo
// =========================================================================

// In nội dung editor qua cửa sổ mới
function inNoiDung() {
    var editor = document.getElementById(rceHienTaiEditorId);
    var tieuDeEl = document.getElementById('postTitle');
    var tieuDe = (tieuDeEl !== null) ? (tieuDeEl.value || 'Nội dung') : 'Nội dung';
    var win = window.open('', '_blank');
    win.document.write('<!DOCTYPE html><html><head><title>' + escapeHtml(tieuDe) + '</title>');
    win.document.write('<style>body{font-family:system-ui,sans-serif;max-width:800px;margin:40px auto;padding:0 20px;color:#1a2040;line-height:1.75;}');
    win.document.write('h1{font-size:1.6rem;font-weight:800;}h2{font-size:1.3rem;font-weight:800;border-bottom:2px solid #eee;padding-bottom:8px;}');
    win.document.write('h3{font-size:1.08rem;font-weight:700;}table{width:100%;border-collapse:collapse;margin:16px 0;}');
    win.document.write('th,td{border:1px solid #ddd;padding:8px 12px;}th{background:#f5f5f5;font-weight:700;}');
    win.document.write('blockquote{border-left:4px solid #0d9488;background:#f0fdfa;padding:12px 16px;margin:16px 0;font-style:italic;}');
    win.document.write('img{max-width:100%;height:auto;}a{color:#0061ff;}</style></head><body>');
    win.document.write('<h1>' + escapeHtml(tieuDe) + '</h1>');
    win.document.write(editor.innerHTML);
    win.document.write('</body></html>');
    win.document.close();
    win.print();
}

// =========================================================================
// WORD COUNT — đếm số từ và ký tự trong editor
// =========================================================================

// Cập nhật bộ đếm từ/ký tự trên toolbar
function capNhatDemTu() {
    var editor = document.getElementById(rceHienTaiEditorId);
    var counter = document.getElementById('rceWordCount');
    if (editor === null || counter === null) return;
    var text = editor.innerText || '';
    text = text.replace(/\s+/g, ' ').trim();
    var soKyTu = text.length;
    var soTu = 0;
    if (text !== '') {
        soTu = text.split(/\s+/).length;
    }
    counter.textContent = soTu + ' từ | ' + soKyTu + ' ký tự';
}

// =========================================================================
// VIDEO YOUTUBE — chèn iframe responsive từ URL YouTube
// =========================================================================

// Chèn video YouTube vào editor từ URL
window.chenVideoYoutube = function() {
    var url = document.getElementById('rceVideoUrl').value.trim();
    if (url === '') { showToast('warning', 'Vui lòng nhập URL video YouTube'); return; }
    var videoId = trichXuatYoutubeId(url);
    if (videoId === null) { showToast('error', 'URL YouTube không hợp lệ'); return; }
    document.getElementById('rceDialogVideo').style.display = 'none';
    var editor = document.getElementById(rceHienTaiEditorId);
    editor.focus();
    khoiPhucViTriConTro();
    var iframeHtml = '<div style="position:relative;padding-bottom:56.25%;height:0;overflow:hidden;margin:20px 0;border-radius:8px;">';
    iframeHtml += '<iframe src="https://www.youtube.com/embed/' + escapeHtml(videoId) + '" ';
    iframeHtml += 'style="position:absolute;top:0;left:0;width:100%;height:100%;border:none;" ';
    iframeHtml += 'allowfullscreen></iframe></div><p><br></p>';
    document.execCommand('insertHTML', false, iframeHtml);
    capNhatPreview();
    luuTrangThaiEditor();
};

// Đóng dialog video
window.dongDialogVideo = function() {
    document.getElementById('rceDialogVideo').style.display = 'none';
};

// Trích xuất video ID từ các dạng URL YouTube phổ biến
function trichXuatYoutubeId(url) {
    var patterns = [
        /(?:youtube\.com\/watch\?v=)([a-zA-Z0-9_-]{11})/,
        /(?:youtu\.be\/)([a-zA-Z0-9_-]{11})/,
        /(?:youtube\.com\/embed\/)([a-zA-Z0-9_-]{11})/,
        /(?:youtube\.com\/v\/)([a-zA-Z0-9_-]{11})/
    ];
    for (var i = 0; i < patterns.length; i = i + 1) {
        var match = url.match(patterns[i]);
        if (match !== null && match[1] !== undefined) return match[1];
    }
    return null;
}

// =========================================================================
// COLOR PICKER — chọn màu chữ hoặc màu nền cho text
// =========================================================================

// Mở dialog chọn màu — loaiMau = 'foreColor' hoặc 'hiliteColor'
function moDialogMau(loaiMau) {
    rceMauDangChon = loaiMau;
    var title = (loaiMau === 'foreColor') ? 'Chọn màu chữ' : 'Chọn màu nền chữ';
    document.getElementById('rceColorTitle').textContent = title;
    document.getElementById('rceDialogColor').style.display = 'flex';
}

// Áp dụng màu đã chọn vào editor
window.apDungMau = function(mau) {
    document.getElementById('rceDialogColor').style.display = 'none';
    var editor = document.getElementById(rceHienTaiEditorId);
    editor.focus();
    khoiPhucViTriConTro();
    document.execCommand(rceMauDangChon, false, mau);
    capNhatPreview();
    luuTrangThaiEditor();
};

// Xóa màu (reset về mặc định)
window.xoaMau = function() {
    document.getElementById('rceDialogColor').style.display = 'none';
    var editor = document.getElementById(rceHienTaiEditorId);
    editor.focus();
    khoiPhucViTriConTro();
    document.execCommand('removeFormat', false, null);
    capNhatPreview();
    luuTrangThaiEditor();
};

// Đóng dialog màu
window.dongDialogColor = function() {
    document.getElementById('rceDialogColor').style.display = 'none';
};

// =========================================================================
// SPECIAL CHARACTERS — ký tự đặc biệt y khoa / khoa học
// =========================================================================

// Chèn ký tự đặc biệt vào editor
window.chenKyTuDacBiet = function(kyTu) {
    document.getElementById('rceDialogSpecialChar').style.display = 'none';
    var editor = document.getElementById(rceHienTaiEditorId);
    editor.focus();
    khoiPhucViTriConTro();
    document.execCommand('insertText', false, kyTu);
    capNhatPreview();
    luuTrangThaiEditor();
};

// Đóng dialog ký tự đặc biệt
window.dongDialogSpecialChar = function() {
    document.getElementById('rceDialogSpecialChar').style.display = 'none';
};

// =========================================================================
// ALERT BOX — hộp cảnh báo/ghi chú y khoa (info, warning, success, danger)
// =========================================================================

// Chèn hộp cảnh báo y khoa vào editor
window.chenAlertBox = function(loai) {
    var noiDung = document.getElementById('rceAlertContent').value.trim();
    if (noiDung === '') { showToast('warning', 'Vui lòng nhập nội dung cảnh báo'); return; }
    document.getElementById('rceDialogAlert').style.display = 'none';
    var editor = document.getElementById(rceHienTaiEditorId);
    editor.focus();
    khoiPhucViTriConTro();

    var mauNen = '#e8f5e9'; var mauVien = '#4caf50'; var icon = 'fas fa-check-circle'; var mauIcon = '#2e7d32';
    if (loai === 'warning') { mauNen = '#fff8e1'; mauVien = '#ff9800'; icon = 'fas fa-exclamation-triangle'; mauIcon = '#e65100'; }
    if (loai === 'danger') { mauNen = '#ffebee'; mauVien = '#f44336'; icon = 'fas fa-times-circle'; mauIcon = '#c62828'; }
    if (loai === 'info') { mauNen = '#e3f2fd'; mauVien = '#2196f3'; icon = 'fas fa-info-circle'; mauIcon = '#1565c0'; }

    var alertHtml = '<div style="background:' + mauNen + ';border-left:4px solid ' + mauVien + ';padding:14px 18px;border-radius:0 8px 8px 0;margin:16px 0;display:flex;gap:10px;align-items:flex-start;">';
    alertHtml += '<i class="' + icon + '" style="color:' + mauIcon + ';margin-top:2px;flex-shrink:0;"></i>';
    alertHtml += '<div>' + escapeHtml(noiDung) + '</div></div><p><br></p>';
    document.execCommand('insertHTML', false, alertHtml);
    capNhatPreview();
    luuTrangThaiEditor();
};

// Đóng dialog alert
window.dongDialogAlert = function() {
    document.getElementById('rceDialogAlert').style.display = 'none';
};

// =========================================================================
// TOGGLE HTML SOURCE — xem/sửa mã HTML gốc
// =========================================================================

// Bật/tắt chế độ xem mã HTML gốc
function batTatCheDoChuoiHtml() {
    var editor = document.getElementById(rceHienTaiEditorId);
    if (dangXemHtmlSource === false) {
        // Chuyển sang chế độ xem HTML source
        var htmlContent = editor.innerHTML;
        editor.innerText = htmlContent;
        editor.style.fontFamily = "'Consolas', monospace";
        editor.style.fontSize = '.82rem';
        editor.style.whiteSpace = 'pre-wrap';
        editor.style.background = '#1e293b';
        editor.style.color = '#e2e8f0';
        dangXemHtmlSource = true;
    } else {
        // Chuyển về chế độ WYSIWYG
        var textContent = editor.innerText;
        editor.innerHTML = textContent;
        editor.style.fontFamily = '';
        editor.style.fontSize = '';
        editor.style.whiteSpace = '';
        editor.style.background = '';
        editor.style.color = '';
        dangXemHtmlSource = false;
        capNhatPreview();
        luuTrangThaiEditor();
    }
}

// =========================================================================
// UNDO / REDO — stack lưu trạng thái HTML
// =========================================================================

// Lưu trạng thái hiện tại của editor vào undo stack
function luuTrangThaiEditor() {
    var editor = document.getElementById(rceHienTaiEditorId);
    if (editor === null) return;
    var html = editor.innerHTML;
    if (editorUndoStack.length > 0 && editorUndoStack[editorUndoStack.length - 1] === html) return;
    editorUndoStack.push(html);
    if (editorUndoStack.length > editorUndoMax) {
        editorUndoStack.shift();
    }
    editorRedoStack = [];
}

// Hoàn tác — quay lại trạng thái trước đó
function hoanTacEditor() {
    if (editorUndoStack.length <= 1) return;
    var editor = document.getElementById(rceHienTaiEditorId);
    var current = editorUndoStack.pop();
    editorRedoStack.push(current);
    editor.innerHTML = editorUndoStack[editorUndoStack.length - 1];
    capNhatPreview();
}

// Làm lại — quay tới trạng thái đã hoàn tác
function lamLaiEditor() {
    if (editorRedoStack.length === 0) return;
    var editor = document.getElementById(rceHienTaiEditorId);
    var html = editorRedoStack.pop();
    editorUndoStack.push(html);
    editor.innerHTML = html;
    capNhatPreview();
}

// =========================================================================
// PREVIEW — đồng bộ nội dung editor sang panel xem trước
// =========================================================================

// Cập nhật panel xem trước — đồng bộ nội dung từ editor sang preview
function capNhatPreview() {
    var editor = document.getElementById(rceHienTaiEditorId);
    var preview = document.getElementById(rceHienTaiPreviewId);
    if (editor !== null && preview !== null) {
        preview.innerHTML = editor.innerHTML;
    }
}

// Debounce cập nhật preview khi gõ phím (200ms)
function debounceCapNhatPreview() {
    if (editorDebounceTimer !== null) {
        clearTimeout(editorDebounceTimer);
    }
    editorDebounceTimer = setTimeout(function() {
        capNhatPreview();
        luuTrangThaiEditor();
        capNhatDemTu();
    }, 200);
}

// =========================================================================
// VIEW MODE — chuyển chế độ hiển thị split/editor/preview
// =========================================================================

// Chuyển chế độ hiển thị editor: split / editor / preview
window.chuyenCheDo = function(mode) {
    var container = document.getElementById('editorSplitContainer');
    container.classList.remove('editor-only', 'preview-only');
    if (mode === 'editor') container.classList.add('editor-only');
    if (mode === 'preview') {
        container.classList.add('preview-only');
        capNhatPreview();
    }
    var btns = document.querySelectorAll('.rce-view-btn');
    for (var i = 0; i < btns.length; i = i + 1) {
        btns[i].classList.remove('active');
    }
    var modeMap = { split: 0, editor: 1, preview: 2 };
    if (modeMap[mode] !== undefined && btns[modeMap[mode]] !== undefined) {
        btns[modeMap[mode]].classList.add('active');
    }
};

// Mở xem trước toàn màn hình
window.moPreviewFullscreen = function() {
    var editor = document.getElementById(rceHienTaiEditorId);
    var overlay = document.getElementById('rceFullscreenPreview');
    var content = document.getElementById('rceFullscreenContent');
    if (editor !== null && content !== null) {
        content.innerHTML = editor.innerHTML;
    }
    overlay.style.display = 'block';
};

// Đóng xem trước toàn màn hình
window.dongPreviewFullscreen = function() {
    document.getElementById('rceFullscreenPreview').style.display = 'none';
};

// =========================================================================
// METADATA — thu gọn/mở rộng phần thông tin phụ
// =========================================================================

// Thu gọn/mở rộng phần thông tin metadata bài viết
window.toggleMetadata = function(el) {
    var body = document.getElementById('rceMetadataBody');
    if (body.classList.contains('collapsed')) {
        body.classList.remove('collapsed');
        el.classList.remove('collapsed');
    } else {
        body.classList.add('collapsed');
        el.classList.add('collapsed');
    }
};

// =========================================================================
// PASTE HANDLER — xử lý paste, giữ tags cơ bản, strip style/class
// =========================================================================

// Xử lý paste — strip formatting, giữ lại tags cơ bản
function xuLyPasteEditor(event) {
    event.preventDefault();
    var clipboardData = event.clipboardData || window.clipboardData;
    var pastedHtml = clipboardData.getData('text/html');
    var pastedText = clipboardData.getData('text/plain');

    if (pastedHtml !== '' && pastedHtml !== undefined) {
        var temp = document.createElement('div');
        temp.innerHTML = pastedHtml;
        var scripts = temp.querySelectorAll('script, style, link, meta');
        for (var s = 0; s < scripts.length; s = s + 1) {
            scripts[s].parentNode.removeChild(scripts[s]);
        }
        var allElements = temp.querySelectorAll('*');
        for (var e = 0; e < allElements.length; e = e + 1) {
            allElements[e].removeAttribute('style');
            allElements[e].removeAttribute('class');
            allElements[e].removeAttribute('id');
        }
        document.execCommand('insertHTML', false, temp.innerHTML);
    } else if (pastedText !== '') {
        document.execCommand('insertText', false, pastedText);
    }
    capNhatPreview();
    luuTrangThaiEditor();
}

// =========================================================================
// BOOTSTRAP — khởi tạo editor, gắn event listeners
// =========================================================================

// Khởi tạo event listeners cho rich content editor — gọi trong DOMContentLoaded
function khoiTaoRichEditor() {
    khoiTaoEditorToolbar();
    var editorArea = document.getElementById('rceEditorArea');
    if (editorArea !== null) {
        editorArea.addEventListener('input', debounceCapNhatPreview);
        editorArea.addEventListener('paste', xuLyPasteEditor);
        // Ctrl+Z/Y override cho custom undo/redo
        editorArea.addEventListener('keydown', function(e) {
            if (e.ctrlKey === true && e.key === 'z') {
                e.preventDefault();
                hoanTacEditor();
            }
            if (e.ctrlKey === true && e.key === 'y') {
                e.preventDefault();
                lamLaiEditor();
            }
        });
    }
}
