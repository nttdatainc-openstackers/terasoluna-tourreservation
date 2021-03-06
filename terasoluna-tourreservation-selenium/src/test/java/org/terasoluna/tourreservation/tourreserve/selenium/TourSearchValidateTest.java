/*
 * Copyright (C) 2013-2014 terasoluna.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.terasoluna.tourreservation.tourreserve.selenium;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.terasoluna.gfw.common.date.DateFactory;
import org.terasoluna.tourreservation.tourreserve.common.FunctionTestSupport;
import org.terasoluna.tourreservation.tourreserve.common.constants.MessageKeys;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:seleniumContext.xml" })
public class TourSearchValidateTest extends FunctionTestSupport {

    WebDriver driver;

    @Inject
    DateFactory dateFactory;

    @Value("${selenium.baseUrl}")
    String baseUrl;

    public TourSearchValidateTest() {
    }

    @Before
    public void setUp() {
        driver = createDefaultLocaleDriver();
    }

    @Test
    public void testRequiredValidate() {
        driver.get(baseUrl + "/terasoluna-tourreservation-web");

        driver.findElement(
                By.xpath("//input[@value='"
                        + getMessage(MessageKeys.LABEL_TR_MENU_LOGINBTNMESSAGE)
                        + "']")).click();

        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys("00000001");
        driver.findElement(
                By.xpath("//input[@value='"
                        + getMessage(MessageKeys.LABEL_TR_COMMON_LOGIN) + "']"))
                .click();

        driver.findElement(
                By.xpath("//input[@value='"
                        + getMessage(MessageKeys.LABEL_TR_MENU_SEARCHBTNMESSAGE)
                        + "']")).click();

        new Select(driver.findElement(By.id("depCode"))).selectByValue("");
        new Select(driver.findElement(By.id("arrCode"))).selectByValue("01");
        driver.findElement(
                By.xpath("//input[@value='"
                        + getMessage(MessageKeys.LABEL_TR_COMMON_SEARCH) + "']"))
                .click();

        assertEquals(
                getMessage(MessageKeys.ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_NOTEMPTY_MESSAGE).replace("{0}", getMessage(MessageKeys.DEPCODE)),
                driver.findElement(By.id("tourInfoSearchCriteria.errors"))
                        .getText());

        new Select(driver.findElement(By.id("depCode"))).selectByValue("01");
        new Select(driver.findElement(By.id("arrCode"))).selectByValue("");
        driver.findElement(
                By.xpath("//input[@value='"
                        + getMessage(MessageKeys.LABEL_TR_COMMON_SEARCH) + "']"))
                .click();

        assertEquals(
                getMessage(MessageKeys.ORG_HIBERNATE_VALIDATOR_CONSTRAINTS_NOTEMPTY_MESSAGE).replace("{0}", getMessage(MessageKeys.ARRCODE)),
                driver.findElement(By.id("tourInfoSearchCriteria.errors"))
                        .getText());
    }

    @Test
    public void testDateValidate() {
        driver.get(baseUrl + "/terasoluna-tourreservation-web");

        driver.findElement(
                By.xpath("//input[@value='"
                        + getMessage(MessageKeys.LABEL_TR_MENU_SEARCHBTNMESSAGE)
                        + "']")).click();

        new Select(driver.findElement(By.id("depCode"))).selectByValue("01");
        new Select(driver.findElement(By.id("arrCode"))).selectByValue("01");

        int currentYear = dateFactory.newDateTime().getYear();

        new Select(driver.findElement(By.id("depYear"))).selectByValue(String
                .valueOf(currentYear));
        new Select(driver.findElement(By.id("depMonth"))).selectByValue("2");
        new Select(driver.findElement(By.id("depDay"))).selectByValue("30");

        driver.findElement(
                By.xpath("//input[@value='"
                        + getMessage(MessageKeys.LABEL_TR_COMMON_SEARCH) + "']"))
                .click();

        assertEquals(getMessage(MessageKeys.INCORRECTDATE_INPUTDATE),
                driver.findElement(By.id("tourInfoSearchCriteria.errors"))
                        .getText());
    }

    @After
    public void tearDown() {
        driver.quit();
    }
}
