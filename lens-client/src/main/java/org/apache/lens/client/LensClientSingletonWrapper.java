/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.lens.client;

import org.apache.lens.client.exceptions.LensClientServerConnectionException;

import lombok.extern.slf4j.Slf4j;

/**
 * The Enum LensClientSingletonWrapper.
 */
@Slf4j
public class LensClientSingletonWrapper {

  /** The instance. */
  public static class InstanceHolder {
    public static final LensClientSingletonWrapper INSTANCE = new LensClientSingletonWrapper();
  }

  public static LensClientSingletonWrapper instance() {
    return InstanceHolder.INSTANCE;
  }

  /** The client. */
  private LensClient client;

  /** The Constant MAX_RETRIES. */
  private static final int MAX_RETRIES = 3;

  /**
   * Instantiates a new lens client singleton wrapper.
   */
  LensClientSingletonWrapper() {
  }

  /**
   * Explain failed attempt.
   *
   * @param e the e
   */
  public void explainFailedAttempt(LensClientServerConnectionException e) {
    log.error("failed login attempt", e);
    switch (e.getErrorCode()) {
    case 401:
      System.console().printf("username/password combination incorrect.\n");
      break;
    case 500:
      System.console().printf("server unresponsive, Returned error code 500\n");
      break;
    default:
      System.console().printf("ERROR: " + e.getMessage() + "\n");
    }
  }

  public LensClient getClient() {
    if (client == null) {
      try {
        client = new LensClient();
      } catch (LensClientServerConnectionException e) {
        if (e.getErrorCode() != 401) {
          explainFailedAttempt(e);
          throw e;
        }
        // Connecting without password prompt failed.
        for (int i = 0; i < MAX_RETRIES; i++) {
          try {
            client = new LensClient(Credentials.prompt());
            break;
          } catch (LensClientServerConnectionException lensClientServerConnectionException) {
            explainFailedAttempt(lensClientServerConnectionException);
            if (i == MAX_RETRIES - 1) {
              throw lensClientServerConnectionException;
            }
          }
        }
      }
    }
    return client;
  }

  public void setClient(LensClient client) {
    this.client = client;
  }

}
