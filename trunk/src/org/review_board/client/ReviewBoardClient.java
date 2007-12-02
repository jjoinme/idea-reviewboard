package org.review_board.client;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.json.JSONObject;
import org.review_board.client.json.Group;
import org.review_board.client.json.Repository;
import org.review_board.client.json.Response;
import org.review_board.client.json.User;
import org.review_board.client.request.GroupsRequest;
import org.review_board.client.request.LoginRequest;
import org.review_board.client.request.RepositoriesRequest;
import org.review_board.client.request.RepositoryInfoRequest;
import org.review_board.client.request.RequestFactory;
import org.review_board.client.request.ReviewBoardRequest;
import org.review_board.client.request.UsersRequest;
import org.review_board.idea.plugin.settings.ProjectSettings;
import org.review_board.idea.plugin.settings.UserSettings;

public class ReviewBoardClient
{
    private final HttpClient m_httpClient;

    private RequestFactory m_requestFactory;

    private String m_username = "";

    private String m_password = "";

    private String m_uri = "";

    public ReviewBoardClient( final String username, final String password,
        final String uri )
    {
        m_username = username;
        m_password = password;
        m_uri = uri;
        MultiThreadedHttpConnectionManager manager =
            new MultiThreadedHttpConnectionManager();
        m_httpClient = new HttpClient( manager );
        m_requestFactory = new RequestFactory( uri );
    }

    public ReviewBoardClient( final UserSettings userSettings,
        final ProjectSettings projectSettings )
    {
        this( userSettings.getUsername(), userSettings.getPassword(),
            projectSettings.getServerUrl() );
    }

    public ArrayList<User> getUsers() throws ReviewBoardException
    {
        final UsersRequest request = m_requestFactory.getUsersRequest();
        processRequest( request );
        return request.getUsers();
    }

    public ArrayList<Group> getGroups() throws ReviewBoardException
    {
        final GroupsRequest request = m_requestFactory.getGroupsRequest();
        processRequest( request );
        return request.getGroups();
    }

    public ArrayList<Repository> getRepositories() throws ReviewBoardException
    {
        final RepositoriesRequest request = m_requestFactory.getRepositoriesRequest();
        processRequest( request );
        return request.getRepositories();
    }

    public JSONObject getRepositoryInfo( final int repositoryId )
        throws ReviewBoardException
    {
        final RepositoryInfoRequest request =
            m_requestFactory.getRepositoryInfoRequest( repositoryId );
        processRequest( request );
        return request.getRepositoryInfo();
    }

    public void login() throws ReviewBoardException
    {
        final LoginRequest request =
            m_requestFactory.getLoginRequest( m_username, m_password );
        processRequest( request );
    }

    private void processRequest( final ReviewBoardRequest request )
        throws ReviewBoardException
    {
        try
        {
            final Response response = request.execute( m_httpClient );

            if ( response.isFailure() )
            {
                if ( response.isNotLoggedInFailure() )
                {
                    login();
                    processRequest( request );
                }
                else
                {
                    throw new ReviewBoardException(
                        "Error from server: " + response.getErrorMessage() );
                }
            }
        }
        catch ( IOException e )
        {
            throw new ReviewBoardException( e );
        }
    }

    public String getUsername()
    {
        return m_username;
    }

    public void setUsername( final String username )
    {
        m_username = username;
    }

    public String getPassword()
    {
        return m_password;
    }

    public void setPassword( final String password )
    {
        m_password = password;
    }

    public String getUri()
    {
        return m_uri;
    }

    public void setUri( final String uri )
    {
        m_uri = uri;
        m_requestFactory = new RequestFactory( uri );
    }
}
