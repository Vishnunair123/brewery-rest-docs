package guru.springframework.msscbrewery.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.msscbrewery.services.BeerService;
import guru.springframework.msscbrewery.web.model.BeerDto;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;

@ExtendWith(value = RestDocumentationExtension.class)
@AutoConfigureRestDocs(uriScheme = "https",uriHost = "dev.springframework.guru",uriPort = 80)
@WebMvcTest(BeerController.class)
//@ComponentScan(basePackages = "guru.springframework.msscbrewery.web.mappers")
public class BeerControllerTest {

    @MockBean
    BeerService beerService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void getBeer() throws Exception {
        given(beerService.getBeerById(any(UUID.class))).willReturn(setUp());

        mockMvc.perform(get("/api/v1/beer/{beerId}?isCold" , UUID.randomUUID().toString())
        		.param("isCold", "yes")
        		.accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("v1/beer-get", pathParameters(
                		parameterWithName("beerId").description("UUID of desired beer to get")),
                		responseFields(
                				fieldWithPath("id").description("Id of Beer").type(UUID.class),
                                fieldWithPath("beerName").description("Beer Name"),
                                fieldWithPath("beerStyle").description("Beer Style"),
                                fieldWithPath("upc").description("UPC of Beer"),
                                fieldWithPath("createdDate").description("Date Created").type(OffsetDateTime.class),
                                fieldWithPath("lastUpdatedDate").description("Date Updated").type(OffsetDateTime.class)
                				)));
    }

    @Test
    public void handlePost() throws Exception {
        //given
        BeerDto beerDto = setUp();
        BeerDto savedDto = BeerDto.builder().id(UUID.randomUUID()).beerName("New Beer").build();
        String beerDtoJson = objectMapper.writeValueAsString(beerDto);

        ConstrainedFields fields = new ConstrainedFields(BeerDto.class);
        
        given(beerService.saveNewBeer(any())).willReturn(savedDto);

        mockMvc.perform(post("/api/v1/beer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(beerDtoJson))
                .andExpect(status().isCreated())
                .andDo(document("v1/beer-new", requestFields(
                		fields.withPath("id").ignored(),
                		fields.withPath("beerName").description("Beer Name"),
                		fields.withPath("beerStyle").description("Beer Style"),
                		fields.withPath("upc").description("UPC of Beer"),
                		fields.withPath("createdDate").ignored(),
                		fields.withPath("lastUpdatedDate").ignored()
        				)));
    }

    @Test
    public void handleUpdate() throws Exception {
        //given
        BeerDto beerDto = setUp();
        beerDto.setId(UUID.randomUUID());
        String beerDtoJson = objectMapper.writeValueAsString(beerDto);
        ConstrainedFields fields = new ConstrainedFields(BeerDto.class);
        
        //when
        mockMvc.perform(put("/api/v1/beer/{beerId}",UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(beerDtoJson))
                .andExpect(status().isNoContent())
                .andDo(document("v1/beer-update", requestFields(
                		fields.withPath("id").description("Beer Id"),
                		fields.withPath("beerName").description("Beer Name"),
                		fields.withPath("beerStyle").description("Beer Style"),
                		fields.withPath("upc").description("UPC of Beer"),
                		fields.withPath("createdDate").ignored(),
                		fields.withPath("lastUpdatedDate").ignored()
        				)));
    }
    
    private BeerDto setUp() {
        return BeerDto.builder()
                .beerName("Beer1")
                .beerStyle("PALE_ALE")
                .upc(123456789012L)
                .build();
    }
    
    private static class ConstrainedFields {

        private final ConstraintDescriptions constraintDescriptions;

		ConstrainedFields(Class<?> input) {
            this.constraintDescriptions = new ConstraintDescriptions(input);
        }

        private FieldDescriptor withPath(String path) {
            return fieldWithPath(path).attributes(key("constraints").value(StringUtils
                    .collectionToDelimitedString(this.constraintDescriptions
                            .descriptionsForProperty(path), ". ")));
        }
    }
}