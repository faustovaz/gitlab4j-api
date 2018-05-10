package org.gitlab4j.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.gitlab4j.api.GitLabApi.ApiVersion;
import org.gitlab4j.api.models.Snippet;
import org.gitlab4j.api.models.Visibility;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSnippetsApi {

    private static final String TEST_HOST_URL;
    private static final String TEST_PRIVATE_TOKEN;
    
    static {
        TEST_HOST_URL = TestUtils.getProperty("TEST_HOST_URL");
        TEST_PRIVATE_TOKEN = TestUtils.getProperty("TEST_PRIVATE_TOKEN");
    }

    private static GitLabApi gitLabApi;
	
    @BeforeClass
    public static void setup() {

        String problems = "";

        if (TEST_HOST_URL == null || TEST_HOST_URL.trim().isEmpty()) {
            problems += "TEST_HOST_URL cannot be empty\n";
        }

        if (TEST_PRIVATE_TOKEN == null || TEST_PRIVATE_TOKEN.trim().isEmpty()) {
            problems += "TEST_PRIVATE_TOKEN cannot be empty\n";
        }

        if (problems.isEmpty()) {
            gitLabApi = new GitLabApi(ApiVersion.V4, TEST_HOST_URL, TEST_PRIVATE_TOKEN);
        } else {
            System.err.print(problems);
        }
    }
    
	@Test
	public void testCreate() throws GitLabApiException {
		Snippet snippet = createSnippet(
					new Snippet("A Small Snippet", "Snippet.java", "Java content"));
		assertEquals("A Small Snippet", snippet.getTitle());
		assertEquals("Snippet.java", snippet.getFileName());
		assertNull(snippet.getContent());
		
		deleteSnippet(snippet);
	}
	
	@Test
	public void testDelete() throws GitLabApiException {
		Snippet snippet = createSnippet(new Snippet("A Small Snippet", "Snippet.java", "Java content"));
		deleteSnippet(snippet);
		
		SnippetsApi api = gitLabApi.getSnippetApi();
		List<Snippet> snippets = api.getSnippets();
		boolean found = snippets.stream().anyMatch(
					s -> s.getId().equals(snippet.getId()));
		assertFalse(found);
	}

	@Test
	public void testList() throws GitLabApiException {
		Snippet snippet1 = createSnippet(new Snippet("Snippet 1", "Snippet.java", "Java content"));
		Snippet snippet2 = createSnippet(new Snippet("Snippet 2", "Snippet.java", "Another java content"));
		
		SnippetsApi api = gitLabApi.getSnippetApi();
		List<Snippet> snippets = api.getSnippets();
		
		assertTrue(snippets.size() >= 2);
		assertTrue(snippets.stream().anyMatch(s -> s.getContent().equals("Java content")));
		assertTrue(snippets.stream().anyMatch(s -> s.getContent().equals("Another java content")));

		
		deleteSnippet(snippet1);
		deleteSnippet(snippet2);
	}
	
	@Test
	public void testSnippetContent() throws GitLabApiException {
		Snippet snippet = createSnippet(
				new Snippet("Snippet 1", "Snippet.java", "System.out.println(\"\");"));
		SnippetsApi api = gitLabApi.getSnippetApi();
		String snippetContent = api.getSnippetContent(snippet.getId());
		assertEquals("System.out.println(\"\");", snippetContent);
		deleteSnippet(snippet);
	}
	
	@Test
	public void testRetrieveSnippet() throws GitLabApiException {
		Snippet snippet = createSnippet (new Snippet(
													"Xml Snippet", 
													"file.xml", 
													"<parent><data>1</data></parent>", 
													Visibility.INTERNAL, 
													"Description"));
		
		SnippetsApi api = gitLabApi.getSnippetApi();
		Snippet savedSnippet = api.getSnippet(snippet.getId());
		
		assertEquals("Xml Snippet", savedSnippet.getTitle());
		assertEquals("file.xml", savedSnippet.getFileName());
		assertEquals("<parent><data>1</data></parent>", savedSnippet.getContent());
		assertEquals("Description", savedSnippet.getDescription());
		
		deleteSnippet(savedSnippet);
	}
	
	public void deleteSnippet(Snippet snippet) throws GitLabApiException {
		SnippetsApi api = gitLabApi.getSnippetApi();
		api.deleteSnippet(snippet.getId());
	}
	
	public Snippet createSnippet(Snippet snippet) throws GitLabApiException {
		SnippetsApi api = gitLabApi.getSnippetApi();
		return api.createSnippet(snippet.getTitle(), 
							snippet.getFileName(), 
							snippet.getContent(), 
							snippet.getVisibility(), 
							snippet.getDescription());
	}
	
}
