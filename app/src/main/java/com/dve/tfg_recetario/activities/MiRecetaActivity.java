package com.dve.tfg_recetario.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dve.tfg_recetario.R;
import com.dve.tfg_recetario.adaptador.AdaptadorEtiquetasReceta;
import com.dve.tfg_recetario.adaptador.AdaptadorIngredientesReceta;
import com.dve.tfg_recetario.adaptador.AdaptadorInstruccionesReceta;
import com.dve.tfg_recetario.fragments.InicioFragment;
import com.dve.tfg_recetario.modelo.entidad.Ingrediente;
import com.dve.tfg_recetario.modelo.entidad.LoadDialog;
import com.dve.tfg_recetario.modelo.entidad.Receta;
import com.dve.tfg_recetario.modelo.negocio.GestorMiReceta;
import com.dve.tfg_recetario.modelo.negocio.GestorReceta;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class MiRecetaActivity extends AppCompatActivity {
    private final String TAG = "tag";
    private final String STEP = "step";

    private GestorMiReceta gestorMiReceta;
    private GestorReceta gestorReceta;

    private RecyclerView rvInstrucciones, rvIngredientes, rvEtiquetas;

    private ImageButton btnAtras, btnAddTag, btnAddIngredient, btnAddStep;

    private AlertDialog addInfoDialog;

    private TextView titleDialog;

    private Button cancelBtn, saveBtn;
    private EditText newInfo, newIngredientName, newIngredientAmount, tituloReceta, tiempoReceta, kcalReceta;
    private ImageView imgIngrediente;

    private List<String> listaEtiquetas;
    private List<Ingrediente> listaIngredientes;
    private List<String> listaInstrucciones;

    private AdaptadorEtiquetasReceta adaptadorEtiquetas;
    private AdaptadorIngredientesReceta adaptadorIngredientes;
    private AdaptadorInstruccionesReceta adaptadorInstrucciones;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView imagenReceta;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private Uri imageUrl;
    private Receta receta;
    private Ingrediente ingrediente;
    private ImageView currentImageTarget = null;
    private Button btnSaveRecipe;

    private AlertDialog progressDialog;

    private boolean modificarReceta;

    private boolean cargandoImagenIngrediente = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_receta);

        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        gestorMiReceta = GestorMiReceta.getInstance();
        gestorReceta = GestorReceta.getInstance();
        gestorReceta.setContext(this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(InicioFragment.OBJ_RECETA)) {
            Log.d("SIII", "Sii");
            receta = (Receta) intent.getSerializableExtra(InicioFragment.OBJ_RECETA);
            receta.setEditable(true);
            modificarReceta = true;
        } else {
            Log.d("SIII", "Nooooo");
            receta = new Receta();
            modificarReceta = false;
        }

        ingrediente = new Ingrediente();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        rvEtiquetas = findViewById(R.id.rvEtiquetasRecetaMi);
        rvIngredientes = findViewById(R.id.rvIngredientesRecetaMi);
        rvInstrucciones = findViewById(R.id.rvInstruccionesRecetaMi);
        btnAtras = findViewById(R.id.btn_atras_mi);
        btnAddTag = findViewById(R.id.btn_add_tag);
        btnAddIngredient = findViewById(R.id.btn_add_ingredient);
        btnAddStep = findViewById(R.id.btn_add_step);
        imagenReceta = findViewById(R.id.img_receta_mi);
        tituloReceta = findViewById(R.id.tituloReceta);
        btnSaveRecipe = findViewById(R.id.btn_save_mi_receta);

        initRecyclerViews(modificarReceta);

        initImagePicker();

        initListeners();

        if (modificarReceta) {
            cargarReceta();
        }
    }

    /**
     * Funcion para lanzar el ImagePicker
     */
    private void changeImageReceta() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, getString(R.string.select_picture)));
    }

    /**
     * Carga el dialog para crear tags / steps según los parametros de entrada
     * @param title Parametro que se mostrara en el titulo del dialog
     * @param hint Parametro que se mostrara de hint en el EditText
     * @param length Parametro que se usara como máximo de caracteres para el EditText
     * @param tipo Parametro que se usara para diferenciar entre step y tag
     */
    public void loadDialogModular(String title, String hint, int length, String tipo) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_modular, null);

        cancelBtn = dialogView.findViewById(R.id.cancel_btn_perfil);
        saveBtn = dialogView.findViewById(R.id.save_btn_perfil);
        newInfo = dialogView.findViewById(R.id.new_username_et_dialog);
        titleDialog = dialogView.findViewById(R.id.titulo_dialog);

        if (tipo.equals(STEP)) {
            newInfo.setSingleLine(false);
            newInfo.setMaxLines(Integer.MAX_VALUE);
            newInfo.setMinLines(1);
            newInfo.setHorizontallyScrolling(false);
        }

        titleDialog.setText(title);
        newInfo.setHint(hint);
        saveBtn.setText(getString(R.string.btn_new_info));

        newInfo.setFilters(new InputFilter[] { new InputFilter.LengthFilter(length) });

        configurarDialog(dialogView, Gravity.BOTTOM);

        cancelBtn.setOnClickListener(v -> addInfoDialog.dismiss());

        saveBtn.setOnClickListener(v -> {
            String newUserName = newInfo.getText().toString().trim();
            if(!newUserName.isBlank()){
                if (title.contains(TAG)) {
                    adaptadorEtiquetas.addItem(newUserName);
                } else if (title.contains(STEP)){
                    adaptadorInstrucciones.addItem(newUserName);
                }
                addInfoDialog.dismiss();
            } else {
                Toast.makeText(this, getString(R.string.no_text_vacio), Toast.LENGTH_SHORT).show();
            }
        });

        addInfoDialog.show();
    }

    /**
     * Carga el dialog para crear ingredientes
     */
    public void loadIngredientDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_ingredient, null);
        ingrediente.setImg("");

        cancelBtn = dialogView.findViewById(R.id.cancel_btn_ingredient);
        saveBtn = dialogView.findViewById(R.id.save_btn_ingredient);
        newIngredientName = dialogView.findViewById(R.id.et_new_ingredient_name);
        newIngredientAmount = dialogView.findViewById(R.id.et_new_ingredient_amount);
        imgIngrediente = dialogView.findViewById(R.id.img_ingrediente_crear);

        saveBtn.setText(getString(R.string.btn_new_info));

        configurarDialog(dialogView, Gravity.CENTER);

        imgIngrediente.setOnClickListener(v -> {
            cargandoImagenIngrediente = true;
            currentImageTarget = imgIngrediente;
            changeImageReceta();
        });

        cancelBtn.setOnClickListener(v -> addInfoDialog.dismiss());

        saveBtn.setOnClickListener(v -> {
            String txtIngredientName = newIngredientName.getText().toString().trim();
            String txtIngredientAmount = newIngredientAmount.getText().toString().trim();

            if (txtIngredientName.isBlank() || txtIngredientAmount.isBlank()) {
                Toast.makeText(this, getString(R.string.ningun_campo_vacio), Toast.LENGTH_SHORT).show();
            } else {
                ingrediente.setIngrediente(txtIngredientName);
                ingrediente.setCantidad(txtIngredientAmount);

                Ingrediente ingredienteSubir = new Ingrediente(ingrediente);
                adaptadorIngredientes.addItem(ingredienteSubir);
                addInfoDialog.dismiss();
            }
        });

        addInfoDialog.show();
    }

    /**
     * Inicializador de todos los RecyclerView de la actividad
     */
    private void initRecyclerViews(boolean modReceta) {
        if (modReceta) {
            initRvEtiquetas(receta.getListaEtiquetas());
            initRvIngredientes(receta.getListaIngredientes());
            initRvInstrucciones(receta.getListaInstrucciones());
        } else {
            initRvEtiquetas(new ArrayList<>());
            initRvIngredientes(new ArrayList<>());
            initRvInstrucciones(new ArrayList<>());
        }
    }

    /**
     * Inicializador del RecyclerView de Etiquetas
     */
    private void initRvEtiquetas(List<String> lista) {
        FlexboxLayoutManager layoutManagerEtiquetas = new FlexboxLayoutManager(this);
        layoutManagerEtiquetas.setFlexDirection(FlexDirection.ROW);
        layoutManagerEtiquetas.setFlexWrap(FlexWrap.WRAP);

        rvEtiquetas.setLayoutManager(layoutManagerEtiquetas);

        listaEtiquetas = new ArrayList<>(lista);

        adaptadorEtiquetas = new AdaptadorEtiquetasReceta(this, listaEtiquetas, true, receta);
        rvEtiquetas.setAdapter(adaptadorEtiquetas);
    }

    /**
     * Inicializador del RecyclerView de Ingredientes
     */
    private void initRvIngredientes(List<Ingrediente> lista) {
        GridLayoutManager layoutManagerIngredientes = new GridLayoutManager(this, 2); // 2 columnas

        rvIngredientes.setLayoutManager(layoutManagerIngredientes);

        listaIngredientes = new ArrayList<>(lista);

        adaptadorIngredientes = new AdaptadorIngredientesReceta(listaIngredientes, this, true, true, receta);
        rvIngredientes.setAdapter(adaptadorIngredientes);
    }

    /**
     * Inicializador del RecyclerView de Instrucciones
     */
    private void initRvInstrucciones(List<String> lista) {
        rvInstrucciones.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        listaInstrucciones = new ArrayList<>(lista);
        adaptadorInstrucciones = new AdaptadorInstruccionesReceta(listaInstrucciones, this, true, receta);
        rvInstrucciones.setAdapter(adaptadorInstrucciones);
    }

    /**
     * Inicializador de ImagePicker
     */
    private void initImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUrl = result.getData().getData();
                        if (cargandoImagenIngrediente && currentImageTarget != null) {
                            gestorMiReceta.modificarIngrediente(ingrediente, String.valueOf(imageUrl), PartesIngrediente.IMAGEN.getValor());
                            gestorMiReceta.cargarImagen(this, imageUrl, currentImageTarget, true);
                            cargandoImagenIngrediente = false;
                        } else {
                            gestorReceta.modificarImagen(receta, String.valueOf(imageUrl));
                            gestorMiReceta.cargarImagen(this, imageUrl, imagenReceta, false);
                        }
                    }
                }
        );
    }

    /**
     * Inicializador de todos los listener de la actividad
     */
    private void initListeners() {
        // Listener de boton atras
        btnAtras.setOnClickListener(v -> {
            finish();
        });

        // Listener para cambiar la imagen de la receta
        imagenReceta.setOnClickListener(v -> {
            changeImageReceta();
        });

        // Listener para añadir una nueva etiqueta a la receta
        btnAddTag.setOnClickListener(v -> {
            loadDialogModular(getString(R.string.txt_new_tag), getString(R.string.hint_new_tag), 20, TAG);
        });

        // Listener para añadir un nuevo ingrediente a la receta
        btnAddIngredient.setOnClickListener(v -> {
            loadIngredientDialog();
        });

        // Listener para añadir un nuevo paso a las instrucciones
        btnAddStep.setOnClickListener(v -> {
            loadDialogModular(getString(R.string.txt_new_step), getString(R.string.hint_new_step), 120, STEP);
        });

        btnSaveRecipe.setOnClickListener(v -> {
            loadDialog();
            gestorReceta.uploadReceta(receta, auth, db, storage, respuesta -> {
                runOnUiThread(() -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if (respuesta == 0) {
                        Toast.makeText(this, getString(R.string.recipe_succesfull_upload), Toast.LENGTH_SHORT).show();
                        if (modificarReceta) {
                            Intent data = new Intent();
                            data.putExtra(InicioFragment.OBJ_RECETA, receta);
                            setResult(1, data);
                        }
                        finish();
                    } else if (respuesta == 1) {
                        Toast.makeText(this, getString(R.string.recipe_image_empty), Toast.LENGTH_SHORT).show();
                    } else if (respuesta == 2) {
                        Toast.makeText(this, getString(R.string.recipe_name_empty), Toast.LENGTH_SHORT).show();
                    } else if (respuesta == 3) {
                        Toast.makeText(this, getString(R.string.recipe_tags_minimum), Toast.LENGTH_SHORT).show();
                    } else if (respuesta == 4) {
                        Toast.makeText(this, getString(R.string.recipe_ingredients_minimum), Toast.LENGTH_SHORT).show();
                    } else if (respuesta == 5) {
                        Toast.makeText(this, getString(R.string.recipe_steps_minimum), Toast.LENGTH_SHORT).show();
                    } else if (respuesta == 6) {
                        Toast.makeText(this, getString(R.string.recipe_creation_error), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            });
        });


        // Guarda los cambios de los EditText en un objeto Receta
        modificarReceta();
    }

    /**
     * Funcion para aplicar configuracion basica modular del dialog
     * @param dialogView Vista del dialog a la que aplicar el dialog
     * @param gravity Posición de la pantalla en la que se mostrará el dialog
     */
    private void configurarDialog(View dialogView, int gravity) {

        addInfoDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (addInfoDialog.getWindow() != null) {
            addInfoDialog.getWindow().setGravity(gravity);
            addInfoDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            addInfoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    /**
     * Función que detecta cambios en la activity y modifica la receta con esos cambios
     */
    private void modificarReceta() {
        tituloReceta.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                receta.setNombre(tituloReceta.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Función para mostrar dialogo de carga
     */
    public void loadDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_carga, null);

        progressDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        LoadDialog.getInstance().inicializar(progressDialog);

        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        progressDialog.show();
    }

    private void cargarReceta() {
        if (!receta.getListaEtiquetas().isEmpty()) {
            Glide.with(this)
                    .load(receta.getImagen())
                    .into(imagenReceta);
            tituloReceta.setText(receta.getNombre());
        }
    }

    private enum PartesIngrediente {
        NOMBRE("nombre"),
        CANTIDAD("cantidad"),
        IMAGEN("img");

        private final String valor;

        PartesIngrediente(String valor) {
            this.valor = valor;
        }

        public String getValor() {
            return valor;
        }
    }

    public interface UploadRecetaCallback {
        void onResult(int respuesta);
    }
}
