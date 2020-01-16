/*
 * Copyright (c) 2002-2019, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.mylutece.modules.ldapprovider.business;

/**
 * This class represents the business object LdapUser
 */
public class LdapUser
{
    // Variables declarations
    private int _nUserId;
    private String _strLdapLogin;
    private String _strLdapGuid;
    private String _strLastName;
    private String _strFirstName;
    private String _strEmail;

    /**
     * Initialize the LdapDatabaseUser
     */
    public static void init( )
    {

    }

    /**
     * Returns the UserId
     *
     * @return The UserId
     */
    public int getUserId( )
    {
        return _nUserId;
    }

    /**
     * Sets the UserId
     *
     * @param nUserId
     *            The UserId
     */
    public void setUserId( int nUserId )
    {
        _nUserId = nUserId;
    }

    /**
     * Returns the Ldap Guid
     *
     * @return The Ldap Guid
     */
    public String getLdapGuid( )
    {
        return _strLdapGuid;
    }

    /**
     * Sets the Ldap Guid
     *
     * @param strLdapGuid
     *            The Ldap Guid
     */
    public void setLdapGuid( String strLdapGuid )
    {
        _strLdapGuid = strLdapGuid;
    }

    /**
     * Returns the Ldap Login
     *
     * @return The Ldap Login
     */
    public String getLdapLogin( )
    {
        return _strLdapLogin;
    }

    /**
     * Sets the Ldap Login
     *
     * @param strLdapGuid
     *            The Ldap Login
     */
    public void setLdapLogin( String strLdapLogin )
    {
        _strLdapLogin = strLdapLogin;
    }

    /**
     * Returns the Email
     *
     * @return The Email
     */
    public String getEmail( )
    {
        return _strEmail;
    }

    /**
     * Sets the Email
     *
     * @param strEmail
     *            The email
     */
    public void setEmail( String strEmail )
    {
        _strEmail = strEmail;
    }

    /**
     * Returns user first name
     *
     * @return The first name
     */
    public String getFirstName( )
    {
        return _strFirstName;
    }

    /**
     * Sets the first name
     *
     * @param strFirstName
     *            The first name
     */
    public void setFirstName( String strFirstName )
    {
        _strFirstName = strFirstName;
    }

    /**
     * Returns the last name
     *
     * @return The last name
     */
    public String getLastName( )
    {
        return _strLastName;
    }

    /**
     * Sets the last name
     *
     * @param strLastName
     *            The last name
     */
    public void setLastName( String strLastName )
    {
        _strLastName = strLastName;
    }

}
