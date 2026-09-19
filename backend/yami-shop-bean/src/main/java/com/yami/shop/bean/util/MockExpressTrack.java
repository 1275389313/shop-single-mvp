package com.yami.shop.bean.util;

import com.yami.shop.bean.app.dto.DeliveryDto;
import com.yami.shop.bean.app.dto.DeliveryInfoDto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Plausible kuaidi100-shaped timeline used when {@code KUAIDI100_CUSTOMER}/{@code KEY} are empty.
 */
public final class MockExpressTrack {

    public static final String SOURCE = "mock";
    public static final String DIRECTION_SHIPMENT = "SHIPMENT";
    public static final String DIRECTION_RETURN = "RETURN";
    public static final String MOCK_MESSAGE = "模拟轨迹（未配置快递100密钥，非真实运单）";

    private static final ZoneId CN = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private MockExpressTrack() {
    }

    public static DeliveryDto shipment(String companyName, String trackingNo, Date shipTime, Date now,
                                       boolean signed, String fromPlace, String toPlace) {
        return build(DIRECTION_SHIPMENT, companyName, trackingNo, shipTime, now, signed, fromPlace, toPlace);
    }

    public static DeliveryDto returned(String companyName, String trackingNo, Date shipTime, Date now,
                                       boolean signed, String fromPlace, String toPlace) {
        return build(DIRECTION_RETURN, companyName, trackingNo, shipTime, now, signed, fromPlace, toPlace);
    }

    public static DeliveryDto empty(String companyName, String trackingNo, String direction, String message) {
        DeliveryDto dto = new DeliveryDto();
        dto.setCompanyName(blankToDefault(companyName, "快递"));
        dto.setDvyFlowId(trackingNo == null ? "" : trackingNo);
        dto.setData(Collections.emptyList());
        dto.setMock(Boolean.TRUE);
        dto.setSource(SOURCE);
        dto.setDirection(direction);
        dto.setMessage(message);
        dto.setState("");
        dto.setStateText("");
        return dto;
    }

    private static DeliveryDto build(String direction, String companyName, String trackingNo, Date shipTime, Date now,
                                     boolean signed, String fromPlace, String toPlace) {
        String company = blankToDefault(companyName, "快递");
        String from = blankToDefault(fromPlace, "广州市");
        String to = blankToDefault(toPlace, "目的城市");
        LocalDateTime origin = toLocal(shipTime != null ? shipTime : (now != null ? now : new Date()));
        LocalDateTime current = toLocal(now != null ? now : new Date());

        List<Step> steps = DIRECTION_RETURN.equals(direction)
                ? returnSteps(company, from, to)
                : shipmentSteps(company, from, to);

        List<DeliveryInfoDto> traces = new ArrayList<>();
        String lastState = "1";
        for (Step step : steps) {
            if (step.signedOnly && !signed) {
                continue;
            }
            LocalDateTime at = origin.plusHours(step.offsetHours);
            if (at.isAfter(current) && traces.size() > 0) {
                continue;
            }
            if (at.isAfter(current)) {
                at = current;
            }
            traces.add(info(step.context, at, step.location));
            lastState = step.state;
        }
        Collections.reverse(traces);

        DeliveryDto dto = new DeliveryDto();
        dto.setCompanyName(company);
        dto.setDvyFlowId(trackingNo == null ? "" : trackingNo);
        dto.setData(traces);
        dto.setMock(Boolean.TRUE);
        dto.setSource(SOURCE);
        dto.setDirection(direction);
        dto.setMessage(MOCK_MESSAGE);
        dto.setState(lastState);
        dto.setStateText(stateText(lastState));
        return dto;
    }

    private static List<Step> shipmentSteps(String company, String from, String to) {
        List<Step> steps = new ArrayList<>();
        steps.add(new Step(0, "1", from, company + "已揽收。揽收点：" + from + "商家仓库"));
        steps.add(new Step(3, "0", from, "快件已到达" + from + "转运中心"));
        steps.add(new Step(8, "0", from, "快件已发往" + to + "转运中心"));
        steps.add(new Step(20, "0", to, "快件已到达" + to + "转运中心"));
        steps.add(new Step(26, "5", to, to + "派件中，派件员：模拟员（mock），请保持电话畅通"));
        steps.add(new Step(32, "3", to, "已签收，签收人：本人。感谢使用" + company, true));
        return steps;
    }

    private static List<Step> returnSteps(String company, String from, String to) {
        List<Step> steps = new ArrayList<>();
        steps.add(new Step(0, "1", from, company + "已揽收退货。寄件人处：" + from));
        steps.add(new Step(4, "0", from, "退货已到达" + from + "转运中心"));
        steps.add(new Step(12, "0", from, "退货已发往商家所在地" + to));
        steps.add(new Step(22, "0", to, "退货已到达" + to + "转运中心"));
        steps.add(new Step(28, "5", to, "正在派往商家仓库（" + to + "）"));
        steps.add(new Step(34, "3", to, "商家已签收退货，签收人：仓库。感谢使用" + company, true));
        return steps;
    }

    private static DeliveryInfoDto info(String context, LocalDateTime at, String location) {
        DeliveryInfoDto dto = new DeliveryInfoDto();
        String time = FMT.format(at);
        dto.setContext(context);
        dto.setTime(time);
        dto.setFtime(time);
        dto.setLocation(location);
        return dto;
    }

    public static String stateText(String state) {
        if (state == null) {
            return "";
        }
        return switch (state) {
            case "0" -> "在途";
            case "1" -> "揽收";
            case "2" -> "疑难";
            case "3" -> "已签收";
            case "4" -> "退签";
            case "5" -> "派件";
            case "6" -> "退回";
            default -> "";
        };
    }

    private static LocalDateTime toLocal(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), CN);
    }

    private static String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static final class Step {
        private final int offsetHours;
        private final String state;
        private final String location;
        private final String context;
        private final boolean signedOnly;

        private Step(int offsetHours, String state, String location, String context) {
            this(offsetHours, state, location, context, false);
        }

        private Step(int offsetHours, String state, String location, String context, boolean signedOnly) {
            this.offsetHours = offsetHours;
            this.state = state;
            this.location = location;
            this.context = context;
            this.signedOnly = signedOnly;
        }
    }
}
