/*
 * Copyright 2018 (c) Denis Andreev (lucifer).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package free.lucifer.jtwain.scan;

import free.lucifer.jtwain.Twain;
import free.lucifer.jtwain.TwainScanner;
import free.lucifer.jtwain.exceptions.TwainException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lucifer
 */
public class SourceManager {

    private static SourceManager instance;

    public static SourceManager instance() {
        if (instance == null) {
            instance = new SourceManager();
        }
        return instance;
    }

    public List<Source> getSources() throws TwainException {
        List<Source> ret = new ArrayList<>();

        for (String s : TwainScanner.getScanner().getDeviceNames()) {
            Source source = new Source();
            source.setName(s);

            ret.add(source);
        }

        return ret;
    }

    public void freeResources() {
        Twain.done();
    }

}
