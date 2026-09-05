package com.example.muyinteresanteNoTocar;

import java.util.ArrayList;
import com.example.muyinteresante.util.RemoteOperationPolicy;

public interface iNoticiaRSS {
	void onRecibeNoticiasRSS(ArrayList<NoticiaRSS> listaNoticias);

	default void onFalloNoticiasRSS(RemoteOperationPolicy.FailureAction action) { }
}
