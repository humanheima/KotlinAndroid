package com.hm.dumingwei.kotlinandroid.handbook.thirteen

import java.io.Serializable

/**
 * Created by dmw on 2019/1/7.
 * Desc:
 */
data class Event(
        var id: String? = null,
        var type: String? = null,
        var actor: ActorBean? = null,
        var repo: RepoBean? = null,
        var payload: PayloadBean? = null,
        @com.google.gson.annotations.SerializedName("public")
        var isPublicX: Boolean = false,
        var created_at: String? = null) : Serializable {

    /**
     * id : 8841227318
     * type : IssueCommentEvent
     * actor : {"id":14139200,"login":"humanheima","display_login":"humanheima","gravatar_id":"","url":"https://api.github.com/users/humanheima","avatar_url":"https://avatars.githubusercontent.com/u/14139200?"}
     * repo : {"id":20394581,"name":"daimajia/AndroidImageSlider","url":"https://api.github.com/repos/daimajia/AndroidImageSlider"}
     * payload : {"action":"created","issue":{"url":"https://api.github.com/repos/daimajia/AndroidImageSlider/issues/396","repository_url":"https://api.github.com/repos/daimajia/AndroidImageSlider","labels_url":"https://api.github.com/repos/daimajia/AndroidImageSlider/issues/396/labels{/name}","comments_url":"https://api.github.com/repos/daimajia/AndroidImageSlider/issues/396/comments","events_url":"https://api.github.com/repos/daimajia/AndroidImageSlider/issues/396/events","html_url":"https://github.com/daimajia/AndroidImageSlider/issues/396","id":349040427,"node_id":"MDU6SXNzdWUzNDkwNDA0Mjc=","number":396,"title":"在RecylerView中作为一个Item使用的时候，下拉刷新数据，slider会快速滑动好多个页面","user":{"login":"humanheima","id":14139200,"node_id":"MDQ6VXNlcjE0MTM5MjAw","avatar_url":"https://avatars0.githubusercontent.com/u/14139200?v=4","gravatar_id":"","url":"https://api.github.com/users/humanheima","html_url":"https://github.com/humanheima","followers_url":"https://api.github.com/users/humanheima/followers","following_url":"https://api.github.com/users/humanheima/following{/other_user}","gists_url":"https://api.github.com/users/humanheima/gists{/gist_id}","starred_url":"https://api.github.com/users/humanheima/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/humanheima/subscriptions","organizations_url":"https://api.github.com/users/humanheima/orgs","repos_url":"https://api.github.com/users/humanheima/repos","events_url":"https://api.github.com/users/humanheima/events{/privacy}","received_events_url":"https://api.github.com/users/humanheima/received_events","type":"User","site_admin":false},"labels":[],"state":"open","locked":false,"assignee":null,"assignees":[],"milestone":null,"comments":2,"created_at":"2018-08-09T09:05:47Z","updated_at":"2019-01-07T02:09:13Z","closed_at":null,"author_association":"NONE","body":""},"comment":{"url":"https://api.github.com/repos/daimajia/AndroidImageSlider/issues/comments/451800863","html_url":"https://github.com/daimajia/AndroidImageSlider/issues/396#issuecomment-451800863","issue_url":"https://api.github.com/repos/daimajia/AndroidImageSlider/issues/396","id":451800863,"node_id":"MDEyOklzc3VlQ29tbWVudDQ1MTgwMDg2Mw==","user":{"login":"humanheima","id":14139200,"node_id":"MDQ6VXNlcjE0MTM5MjAw","avatar_url":"https://avatars0.githubusercontent.com/u/14139200?v=4","gravatar_id":"","url":"https://api.github.com/users/humanheima","html_url":"https://github.com/humanheima","followers_url":"https://api.github.com/users/humanheima/followers","following_url":"https://api.github.com/users/humanheima/following{/other_user}","gists_url":"https://api.github.com/users/humanheima/gists{/gist_id}","starred_url":"https://api.github.com/users/humanheima/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/humanheima/subscriptions","organizations_url":"https://api.github.com/users/humanheima/orgs","repos_url":"https://api.github.com/users/humanheima/repos","events_url":"https://api.github.com/users/humanheima/events{/privacy}","received_events_url":"https://api.github.com/users/humanheima/received_events","type":"User","site_admin":false},"created_at":"2019-01-07T02:09:13Z","updated_at":"2019-01-07T02:09:13Z","author_association":"NONE","body":"> 请问你解决这个问题了吗\r\n\r\n没有"}}
     * public : true
     * created_at : 2019-01-07T02:09:13Z
     */

    data class ActorBean(var id: Int = 0,
                         var login: String? = null,
                         var display_login: String? = null,
                         var gravatar_id: String? = null,
                         var url: String? = null,
                         var avatar_url: String? = null) : Serializable {
        /**
         * id : 14139200
         * login : humanheima
         * display_login : humanheima
         * gravatar_id :
         * url : https://api.github.com/users/humanheima
         * avatar_url : https://avatars.githubusercontent.com/u/14139200?
         */
    }

    data class RepoBean(var id: Int = 0,
                        var name: String? = null,
                        var url: String? = null) : Serializable {
        /**
         * id : 20394581
         * name : daimajia/AndroidImageSlider
         * url : https://api.github.com/repos/daimajia/AndroidImageSlider
         */
    }

    data class PayloadBean(var action: String? = null,
                           var issue: IssueBean? = null,
                           var comment: CommentBean? = null) : Serializable {
        /**
         * action : created
         * issue : {"url":"https://api.github.com/repos/daimajia/AndroidImageSlider/issues/396","repository_url":"https://api.github.com/repos/daimajia/AndroidImageSlider","labels_url":"https://api.github.com/repos/daimajia/AndroidImageSlider/issues/396/labels{/name}","comments_url":"https://api.github.com/repos/daimajia/AndroidImageSlider/issues/396/comments","events_url":"https://api.github.com/repos/daimajia/AndroidImageSlider/issues/396/events","html_url":"https://github.com/daimajia/AndroidImageSlider/issues/396","id":349040427,"node_id":"MDU6SXNzdWUzNDkwNDA0Mjc=","number":396,"title":"在RecylerView中作为一个Item使用的时候，下拉刷新数据，slider会快速滑动好多个页面","user":{"login":"humanheima","id":14139200,"node_id":"MDQ6VXNlcjE0MTM5MjAw","avatar_url":"https://avatars0.githubusercontent.com/u/14139200?v=4","gravatar_id":"","url":"https://api.github.com/users/humanheima","html_url":"https://github.com/humanheima","followers_url":"https://api.github.com/users/humanheima/followers","following_url":"https://api.github.com/users/humanheima/following{/other_user}","gists_url":"https://api.github.com/users/humanheima/gists{/gist_id}","starred_url":"https://api.github.com/users/humanheima/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/humanheima/subscriptions","organizations_url":"https://api.github.com/users/humanheima/orgs","repos_url":"https://api.github.com/users/humanheima/repos","events_url":"https://api.github.com/users/humanheima/events{/privacy}","received_events_url":"https://api.github.com/users/humanheima/received_events","type":"User","site_admin":false},"labels":[],"state":"open","locked":false,"assignee":null,"assignees":[],"milestone":null,"comments":2,"created_at":"2018-08-09T09:05:47Z","updated_at":"2019-01-07T02:09:13Z","closed_at":null,"author_association":"NONE","body":""}
         * comment : {"url":"https://api.github.com/repos/daimajia/AndroidImageSlider/issues/comments/451800863","html_url":"https://github.com/daimajia/AndroidImageSlider/issues/396#issuecomment-451800863","issue_url":"https://api.github.com/repos/daimajia/AndroidImageSlider/issues/396","id":451800863,"node_id":"MDEyOklzc3VlQ29tbWVudDQ1MTgwMDg2Mw==","user":{"login":"humanheima","id":14139200,"node_id":"MDQ6VXNlcjE0MTM5MjAw","avatar_url":"https://avatars0.githubusercontent.com/u/14139200?v=4","gravatar_id":"","url":"https://api.github.com/users/humanheima","html_url":"https://github.com/humanheima","followers_url":"https://api.github.com/users/humanheima/followers","following_url":"https://api.github.com/users/humanheima/following{/other_user}","gists_url":"https://api.github.com/users/humanheima/gists{/gist_id}","starred_url":"https://api.github.com/users/humanheima/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/humanheima/subscriptions","organizations_url":"https://api.github.com/users/humanheima/orgs","repos_url":"https://api.github.com/users/humanheima/repos","events_url":"https://api.github.com/users/humanheima/events{/privacy}","received_events_url":"https://api.github.com/users/humanheima/received_events","type":"User","site_admin":false},"created_at":"2019-01-07T02:09:13Z","updated_at":"2019-01-07T02:09:13Z","author_association":"NONE","body":"> 请问你解决这个问题了吗\r\n\r\n没有"}
         */

        data class IssueBean(
                var url: String? = null,
                var repository_url: String? = null,
                var labels_url: String? = null,
                var comments_url: String? = null,
                var events_url: String? = null,
                var html_url: String? = null,
                var id: Int = 0,
                var node_id: String? = null,
                var number: Int = 0,
                var title: String? = null,
                var user: UserBean? = null,
                var state: String? = null,
                var isLocked: Boolean = false,
                var assignee: Any? = null,
                var milestone: Any? = null,
                var comments: Int = 0,
                var created_at: String? = null,
                var updated_at: String? = null,
                var closed_at: Any? = null,
                var author_association: String? = null,
                var body: String? = null,
                var labels: List<*>? = null,
                var assignees: List<*>? = null) : Serializable {
            /**
             * url : https://api.github.com/repos/daimajia/AndroidImageSlider/issues/396
             * repository_url : https://api.github.com/repos/daimajia/AndroidImageSlider
             * labels_url : https://api.github.com/repos/daimajia/AndroidImageSlider/issues/396/labels{/name}
             * comments_url : https://api.github.com/repos/daimajia/AndroidImageSlider/issues/396/comments
             * events_url : https://api.github.com/repos/daimajia/AndroidImageSlider/issues/396/events
             * html_url : https://github.com/daimajia/AndroidImageSlider/issues/396
             * id : 349040427
             * node_id : MDU6SXNzdWUzNDkwNDA0Mjc=
             * number : 396
             * title : 在RecylerView中作为一个Item使用的时候，下拉刷新数据，slider会快速滑动好多个页面
             * user : {"login":"humanheima","id":14139200,"node_id":"MDQ6VXNlcjE0MTM5MjAw","avatar_url":"https://avatars0.githubusercontent.com/u/14139200?v=4","gravatar_id":"","url":"https://api.github.com/users/humanheima","html_url":"https://github.com/humanheima","followers_url":"https://api.github.com/users/humanheima/followers","following_url":"https://api.github.com/users/humanheima/following{/other_user}","gists_url":"https://api.github.com/users/humanheima/gists{/gist_id}","starred_url":"https://api.github.com/users/humanheima/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/humanheima/subscriptions","organizations_url":"https://api.github.com/users/humanheima/orgs","repos_url":"https://api.github.com/users/humanheima/repos","events_url":"https://api.github.com/users/humanheima/events{/privacy}","received_events_url":"https://api.github.com/users/humanheima/received_events","type":"User","site_admin":false}
             * labels : []
             * state : open
             * locked : false
             * assignee : null
             * assignees : []
             * milestone : null
             * comments : 2
             * created_at : 2018-08-09T09:05:47Z
             * updated_at : 2019-01-07T02:09:13Z
             * closed_at : null
             * author_association : NONE
             * body :
             */
        }

        data class CommentBean(
                var url: String? = null,
                var html_url: String? = null,
                var issue_url: String? = null,
                var id: Int = 0,
                var node_id: String? = null,
                var user: UserBean? = null,
                var created_at: String? = null,
                var updated_at: String? = null,
                var author_association: String? = null,
                var body: String? = null) : Serializable {
            /**
             * url : https://api.github.com/repos/daimajia/AndroidImageSlider/issues/comments/451800863
             * html_url : https://github.com/daimajia/AndroidImageSlider/issues/396#issuecomment-451800863
             * issue_url : https://api.github.com/repos/daimajia/AndroidImageSlider/issues/396
             * id : 451800863
             * node_id : MDEyOklzc3VlQ29tbWVudDQ1MTgwMDg2Mw==
             * user : {"login":"humanheima","id":14139200,"node_id":"MDQ6VXNlcjE0MTM5MjAw","avatar_url":"https://avatars0.githubusercontent.com/u/14139200?v=4","gravatar_id":"","url":"https://api.github.com/users/humanheima","html_url":"https://github.com/humanheima","followers_url":"https://api.github.com/users/humanheima/followers","following_url":"https://api.github.com/users/humanheima/following{/other_user}","gists_url":"https://api.github.com/users/humanheima/gists{/gist_id}","starred_url":"https://api.github.com/users/humanheima/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/humanheima/subscriptions","organizations_url":"https://api.github.com/users/humanheima/orgs","repos_url":"https://api.github.com/users/humanheima/repos","events_url":"https://api.github.com/users/humanheima/events{/privacy}","received_events_url":"https://api.github.com/users/humanheima/received_events","type":"User","site_admin":false}
             * created_at : 2019-01-07T02:09:13Z
             * updated_at : 2019-01-07T02:09:13Z
             * author_association : NONE
             * body : > 请问你解决这个问题了吗
             *
             * 没有
             */
        }

        data class UserBean(
                var login: String? = null,
                var id: Int = 0,
                var node_id: String? = null,
                var avatar_url: String? = null,
                var gravatar_id: String? = null,
                var url: String? = null,
                var html_url: String? = null,
                var followers_url: String? = null,
                var following_url: String? = null,
                var gists_url: String? = null,
                var starred_url: String? = null,
                var subscriptions_url: String? = null,
                var organizations_url: String? = null,
                var repos_url: String? = null,
                var events_url: String? = null,
                var received_events_url: String? = null,
                var type: String? = null,
                var isSite_admin: Boolean = false) : Serializable {

            /**
             * login : humanheima
             * id : 14139200
             * node_id : MDQ6VXNlcjE0MTM5MjAw
             * avatar_url : https://avatars0.githubusercontent.com/u/14139200?v=4
             * gravatar_id :
             * url : https://api.github.com/users/humanheima
             * html_url : https://github.com/humanheima
             * followers_url : https://api.github.com/users/humanheima/followers
             * following_url : https://api.github.com/users/humanheima/following{/other_user}
             * gists_url : https://api.github.com/users/humanheima/gists{/gist_id}
             * starred_url : https://api.github.com/users/humanheima/starred{/owner}{/repo}
             * subscriptions_url : https://api.github.com/users/humanheima/subscriptions
             * organizations_url : https://api.github.com/users/humanheima/orgs
             * repos_url : https://api.github.com/users/humanheima/repos
             * events_url : https://api.github.com/users/humanheima/events{/privacy}
             * received_events_url : https://api.github.com/users/humanheima/received_events
             * type : User
             * site_admin : false
             */
        }
    }


}


