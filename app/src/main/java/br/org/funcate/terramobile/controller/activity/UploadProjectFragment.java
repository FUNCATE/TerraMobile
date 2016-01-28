package br.org.funcate.terramobile.controller.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import br.org.funcate.jgpkg.exception.QueryException;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.settings.ServerURLController;
import br.org.funcate.terramobile.controller.activity.settings.SettingsActivity;
import br.org.funcate.terramobile.model.domain.Project;
import br.org.funcate.terramobile.model.exception.InvalidGeopackageException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.service.AppGeoPackageService;
import br.org.funcate.terramobile.model.service.LayersService;
import br.org.funcate.terramobile.model.tilesource.TerraMobileInvalidationHandler;
import br.org.funcate.terramobile.view.ProjectListAdapter;
import br.org.funcate.terramobile.view.UploadLayerListAdapter;

/**
 * DialogFragment to show the user's credentials form on the settings menu
 *
 * Created by marcelo on 5/25/15.
 */
public class UploadProjectFragment extends DialogFragment{
    Project project;
//    private EditText eTServerURL;
    private UploadProjectController controller;

    private UploadLayerListAdapter uploadListAdapter;

    View view=null;
    public static UploadProjectFragment newInstance() {
        UploadProjectFragment fragment = new UploadProjectFragment();

        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        view = inflater.inflate(R.layout.fragment_upload_project, null);
        controller=new UploadProjectController();

       /* if(this.project!=null)
        {
            return null;
        }*/



  //      eTServerURL = (EditText) v.findViewById(R.id.serverURL);

/*        Button btnSave = (Button) v.findViewById(R.id.btnSave);
        Button btnCancel = (Button) v.findViewById(R.id.btnCancel);*/

/*
        String serverUrl = ((SettingsActivity)this.getActivity()).getController().getServerURL();
        if (serverUrl != null)
            eTServerURL.setText(serverUrl);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    urlController.save(eTServerURL.getText().toString());
                    dismiss();
                }
            }
        });
*/

/*        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });*/

        return new AlertDialog.Builder(getActivity())
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                }).setPositiveButton(R.string.upload, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        uploadProject();
                        dismiss();
                    }
                })
                .setView(view)
                .setTitle(R.string.project_upload)
                .setCancelable(true)
                .create();
    }

    public void setProject(Project project)
    {
        this.project=project;
    }

    private void buildLayersList()
    {
        if(this.project!=null)
        {
            try {

                ArrayList<GpkgLayer> layers= AppGeoPackageService.getLayers(this.project, getActivity());

                ArrayList<GpkgLayer> editableLayers = LayersService.getEditableLayers(layers);

                ListView layersList = (ListView)view.findViewById(R.id.layersListView);

                uploadListAdapter = new UploadLayerListAdapter(getActivity(), R.id.layersListView, editableLayers);

                layersList.setAdapter(uploadListAdapter);

            } catch (InvalidGeopackageException e) {
                e.printStackTrace();
            } catch (QueryException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        if (getDialog() == null) {  // Returns mDialog
            // Tells DialogFragment to not use the fragment as a dialog, and so won't try to use mDialog
            setShowsDialog(false);
        }
        buildLayersList();
        super.onActivityCreated(arg0);  // Will now complete and not crash
    }

    private boolean uploadProject()
    {
       return false;
    }
}