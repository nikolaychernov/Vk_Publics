package com.nikolaychernov.vkpublics;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKApiPost;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKPostArray;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String PUBLIC_ID = "vk.krasota";
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.list_posts)
    RecyclerView listPosts;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    LinearLayoutManager layoutManager;

    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        layoutManager = new LinearLayoutManager(this);
        listPosts.setLayoutManager(layoutManager);
        listPosts.setAdapter(new WallPostAdapter(new ArrayList<VKApiPost>()));
        listPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if(dy > 0)  {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            loading = false;
                            Log.v("...", "Last Item Wow !");
                            reloadPosts(totalItemCount);
                        }
                    }
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadPosts(0);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        if (!VKSdk.isLoggedIn())
            VKSdk.login(this, null);
        else {
            reloadPosts(0);
        }
    }

    private void reloadPosts(final int offset) {
        String publicId = PreferenceManager.getDefaultSharedPreferences(this).getString("public_id", "mudakoff");
        VKApi.wall().get(VKParameters.from(VKApiConst.DOMAIN,  publicId, VKApiConst.OFFSET, offset)).executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    List<VKApiPost> posts = (List<VKApiPost>) new VKPostArray().parse(response.json);
                    if (offset == 0) ((WallPostAdapter)listPosts.getAdapter()).clear();
                    ((WallPostAdapter)listPosts.getAdapter()).addItems(posts);
                    loading = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // User passed Authorization
            }
            @Override
            public void onError(VKError error) {
                // User didn't pass Authorization
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class WallPostAdapter extends RecyclerView.Adapter<WallPostAdapter.WallPostViewHolder> {

        List<VKApiPost> data;

        public class WallPostViewHolder extends RecyclerView.ViewHolder {

            private View itemView;
            private ImageView image;
            private ImageView imageLike;
            private ImageView imageComment;
            private TextView textPostContent;
            private TextView textPostDate;
            private TextView buttonLike;
            private TextView buttonComment;
            private TextView buttonRepost;

            public WallPostViewHolder(View view) {
                super(view);
                itemView = view;
                image = (ImageView) view.findViewById(R.id.image);
                imageLike = (ImageView) view.findViewById(R.id.image_like);
                imageComment = (ImageView) view.findViewById(R.id.image_comment);
                textPostContent = (TextView) view.findViewById(R.id.text_post_content);
                textPostDate = (TextView) view.findViewById(R.id.text_post_date);
                buttonLike = (TextView) view.findViewById(R.id.text_like);
                buttonComment = (TextView) view.findViewById(R.id.text_comment);
                buttonRepost = (TextView) view.findViewById(R.id.text_repost);
            }
        }

        public WallPostAdapter(List<VKApiPost> data) {
            this.data = data;
        }

        public void addItems(List<VKApiPost> items){
            data.addAll(items);
            notifyDataSetChanged();
        }

        public void clear(){
            data.clear();
        }

        @Override
        public WallPostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new WallPostViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_wall_post, parent, false));
        }

        @Override
        public void onBindViewHolder(WallPostAdapter.WallPostViewHolder holder, int position) {
            Context context = holder.itemView.getContext();

            VKApiPost post = data.get(position);
            Date now = new Date();
            holder.textPostDate.setText(DateUtils.getRelativeTimeSpanString(
                    post.date * DateUtils.SECOND_IN_MILLIS,
                    now.getTime(),
                    DateUtils.MINUTE_IN_MILLIS));

            int color = post.user_likes ? R.color.colorPrimary : R.color.disabled_grey;
            holder.imageLike.setColorFilter(ContextCompat.getColor(context, color));
            holder.textPostContent.setText(post.text);
            holder.textPostContent.setVisibility(StringUtils.isNotEmpty(post.text) ? View.VISIBLE : View.GONE);
            holder.buttonLike.setText(String.valueOf(post.likes_count));
            holder.buttonComment.setText(String.valueOf(post.comments_count));
            holder.buttonRepost.setText(String.valueOf(post.reposts_count));
            for (VKAttachments.VKApiAttachment attachment : post.attachments) {
                if (attachment.getType().equals(VKAttachments.TYPE_PHOTO) && VkUtils.findLargestPhotoUrl((VKApiPhoto) attachment) != null) {
                    Picasso.with(context)
                            .load(VkUtils.findLargestPhotoUrl((VKApiPhoto) attachment))
                            .into(holder.image);
                    holder.image.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}
