package ru.maizy.ambient7.webapp.tests.servlet

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.webapp.AppConfig
import ru.maizy.ambient7.webapp.servlet.DevicesServlet
import ru.maizy.ambient7.webapp.tests.{ BaseServletTest, JsonAsserts }

class DevicesServletSpec extends BaseServletTest with JsonAsserts {
  override def initServlets(config: AppConfig): Unit = {
    addServlet(new DevicesServlet(config), "/devices")
  }

  "/devices" should "return devices list" in {
    get("/devices") {
      status shouldBe 200
      assertJson(body) {
        """{
          "co2": [
            {
              "id": "one",
              "agent": {
                "name": "main",
                "tags": []
              }
            },
            {
              "id": "two",
              "agent": {
                "name": "agent-name",
                "tags": [
                  {
                    "name": "altitude",
                    "value": "200"
                  },
                  {
                    "name": "place",
                    "value": "livingroom"
                  }
                ]
              }
            }
          ]
        }"""
      }
    }
  }

  "/devices/:id" should "return device info" in {
    get("/devices/two") {
      status shouldBe 200
      assertJson(body) {
        """{
          "id": "two",
          "agent": {
            "name": "agent-name",
            "tags": [
              {
                "name": "altitude",
                "value": "200"
              },
              {
                "name": "place",
                "value": "livingroom"
              }
            ]
          }
        }
        """
      }
    }
  }

  it should "return Not Found for unknown device" in {
    get("/device/not_exist") {
      status shouldBe 404
    }
  }
}
