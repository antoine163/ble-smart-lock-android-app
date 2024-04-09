/***
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 antoine163
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * @file taskApp.h
 * @author antoine163
 * @date 03-04-2024
 * @brief Task of the application
 */

// Include ---------------------------------------------------------------------
#include "taskApp.h"
#include "board.h"
#include "tasks/taskLight/taskLight.h"

#include <FreeRTOS.h>
#include <task.h>

// Global variables ------------------------------------------------------------

// Implemented functions -------------------------------------------------------


// Implemented functions -------------------------------------------------------
void taskAppCode(__attribute__((unused)) void *parameters)
{
    boardEnableIo(true);

    while (1)
    {
        // boardLedOn();
        // vTaskDelay(500 / portTICK_PERIOD_MS);
        // boardLedOff();
        // vTaskDelay(500 / portTICK_PERIOD_MS);

        // taskLightSetColor(COLOR_WHITE, 1500);
        // vTaskDelay(3000 / portTICK_PERIOD_MS);

        // taskLightSetColor(COLOR_OFF, 1500);
        // vTaskDelay(3000 / portTICK_PERIOD_MS);
    }
}