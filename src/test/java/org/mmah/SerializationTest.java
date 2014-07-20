package org.mmah;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import itunes_parser.itunes.MusicLibrary;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mmah.config.JsonConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * Created by karl on 7/4/14.
 */
public class SerializationTest {

    @Test
    public void testLibrarySerialization() throws IOException {
        ObjectMapper objectMapper = new JsonConfig().objectMapper();
        MusicLibrary library = new MusicLibrary();
        objectMapper.writeValue(ByteStreams.nullOutputStream(),library);
    }
}
