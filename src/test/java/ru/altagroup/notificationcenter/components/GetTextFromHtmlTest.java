package ru.altagroup.notificationcenter.components;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import ru.altagroup.notificationcenter.entities.ErrorCode;
import ru.altagroup.notificationcenter.entities.Notice;
import ru.altagroup.notificationcenter.factory.ErrorMessageFactory;
import ru.altagroup.notificationcenter.factory.HealthResetMessageFactory;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest
public class GetTextFromHtmlTest {

    @Autowired
    private ErrorMessageFactory errorMessageFactory;
    @Autowired
    private HealthResetMessageFactory healthResetMessageFactory;

    @RegisterExtension
    static WireMockExtension errorsService = WireMockExtension.newInstance().options(wireMockConfig().port(8090)).build();
    private final String GET_ERROR = "/errors/E200011";

    @Test
    @Disabled
    public void testGetErrorText() {
        errorsService.stubFor(get(urlPathEqualTo(GET_ERROR))
                .willReturn(okForJson(errorCode()).withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, org.springframework.http.MediaType.APPLICATION_JSON_VALUE)));

        long timestamp = DateTime.now().getMillis();
        String stationName = "Station";
        String code = "E200011";
        String html = errorMessageFactory.createHtmlEmailText(timestamp,stationName, code);

        Document doc = Jsoup.parse(html);
        Element link = doc.select(".paragraph").first();
        assert link != null;
        System.out.println(link.text());

        String expectedText = "Короткое замыкание одного из каналов нагрузки модуля коммутации силовой нагрузки (превышение значения тока 20А в течение не менее 40 мс).";
        Assertions.assertEquals(expectedText, link.text());
    }

    @Test
    public void testGetHealthResetText() {
        long timestamp = DateTime.now().getMillis();
        String stationName = "Station";
        String html = healthResetMessageFactory.createHtmlEmailText(timestamp, stationName);

        Document doc = Jsoup.parse(html);
        Element link = doc.select(".paragraph").first();
        assert link != null;
        System.out.println(link.text());

        String expectedText = "Состояние станции Station успешно обновлено";
        Assertions.assertEquals(expectedText, link.text());
    }

    private ErrorCode errorCode() {
        ErrorCode errorCode = new ErrorCode();
        errorCode.setCode("E200011");
        errorCode.setDescription("DOT, короткое замыкание.");
        errorCode.setFullDescription("Короткое замыкание одного из каналов нагрузки модуля коммутации силовой нагрузки (превышение значения тока 20А в течение не менее 40 мс).");
        errorCode.setLevel("Критическая ошибка");
        errorCode.setRecommendations("Отключить питание DOT. Отключить разъемы каналов 1, 2 и 3. С помощью мультиметра измерить сопротивление цепи «фаза-нейтраль» разъема отходящего кабеля питания насосов. Выявленный насос с коротким замыканием вывести из работы.");
        errorCode.setService(Notice.SYSTEM);
        return errorCode;
    }
}
