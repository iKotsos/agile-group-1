package se.chalmers.agile.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.CommitService;

import java.io.IOException;
import java.util.Collection;

import se.chalmers.agile.R;


public class CommitFilesFragment extends ListFragment {

    private String repositoryName, branchName, sha;
    private static final String SHA = "sha_value_of_the_commit";


    public static Fragment newInstance(String repositoryName, String branchName, String sha) {
        Bundle bundle = new Bundle();
        bundle.putString("repositoryName", repositoryName);
        bundle.putString("branchName", branchName);
        bundle.putString(SHA, sha);
        CommitFilesFragment f = new CommitFilesFragment();
        f.setArguments(bundle);
        return f;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repositoryName = getArguments().getString("repositoryName");
        branchName = getArguments().getString("branchName");
        sha = getArguments().getString(SHA);

        // TODO: Change Adapter to display your content
        new FileFetcher().execute(repositoryName, branchName, sha);
    }


    private class FileFetcher extends AsyncTask<String, Void, Collection<CommitFile>>{

        @Override
        protected Collection<CommitFile> doInBackground(String... args) {
            CommitService cs = new CommitService();

            cs.getClient().setOAuth2Token(getActivity().getString(R.string.api_key));

            Log.d("TEST",args[0]);
            Log.d("SHA", args[2]);
            String[] project = args[0].split("/");

            IRepositoryIdProvider repositoryId = RepositoryId.create(project[0], project[1]);

            Collection<CommitFile> files = null;

            try {
                files = cs.getCommit(repositoryId, sha).getFiles();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return files;
        }


        @Override
        protected void onPostExecute(Collection<CommitFile> commitFiles) {
            super.onPostExecute(commitFiles);
            setListAdapter(new FilesAdapter(getActivity(),
                    android.R.id.list,
                    commitFiles.toArray(new CommitFile[commitFiles.size()])));
        }
    }




    private class FilesAdapter extends ArrayAdapter<CommitFile> {

        private CommitFile[] commitFiles = null;

        public FilesAdapter(Context context, int resource, CommitFile[] commitFiles) {
            super(context, resource);
            this.commitFiles = commitFiles;

        }

        @Override
        public int getCount() {
            return commitFiles.length;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = ((Activity) getActivity()).getLayoutInflater();
                row = inflater.inflate(R.layout.files_row, parent, false);
            }

            TextView title = (TextView) row.findViewById(R.id.fileTitle);
            String s  = commitFiles[position].getFilename();
            title.setText(s.substring(s.lastIndexOf("/")+1));

            return row;
        }
    }









}
