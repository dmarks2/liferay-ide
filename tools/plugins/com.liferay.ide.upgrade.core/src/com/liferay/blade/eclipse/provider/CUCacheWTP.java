/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liferay.blade.eclipse.provider;

import com.liferay.blade.api.CUCache;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.jsp.core.internal.java.JSPTranslator;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.osgi.service.component.annotations.Component;

/**
 * @author Gregory Amerson
 */
@Component(
	property = {
		"type=jsp"
	},
	service = CUCache.class
)
@SuppressWarnings("restriction")
public class CUCacheWTP extends BaseCUCache implements CUCache<JSPTranslationPrime> {

	private static final Object _lock = new Object();
	private static final Map<File, Long> _fileModifiedTimeMap = new HashMap<>();
	private static final Map<File, WeakReference<JSPTranslationPrime>> _jspTranslationMap = new HashMap<>();

	@Override
	public JSPTranslationPrime getCU(File file, Supplier<char[]> javavSource) {
		JSPTranslationPrime retval = null;

		try {
			synchronized (_lock) {
				Long lastModified = _fileModifiedTimeMap.get(file);

				if (lastModified != null && lastModified.equals(file.lastModified())) {
					retval = _jspTranslationMap.get(file).get();
				}

				if (retval == null) {
					JSPTranslationPrime newTranslation = createJSPTranslation(file);

					_fileModifiedTimeMap.put(file, file.lastModified());
					_jspTranslationMap.put(file, new WeakReference<JSPTranslationPrime>(newTranslation));

					retval = newTranslation;
				}
			}
		}
		catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		return retval;
	}

	@Override
	public void unget(File file) {
		synchronized (_lock) {
			_fileModifiedTimeMap.remove(file);
			_jspTranslationMap.remove(file);
		}
	}

	private JSPTranslationPrime createJSPTranslation(File file) {
		IDOMModel jspModel = null;

		try {
			// try to find the file in the current workspace, if it can't find
			// it then fall back to copy

			final IFile jspFile = getIFile(file);

			jspModel = (IDOMModel) StructuredModelManager.getModelManager().getModelForRead(jspFile);
			final IDOMDocument domDocument = jspModel.getDocument();
			final IDOMNode domNode = (IDOMNode) domDocument.getDocumentElement();

			final IProgressMonitor npm = new NullProgressMonitor();
			final JSPTranslator translator = new JSPTranslatorPrime();

			if (domNode != null) {
				translator.reset((IDOMNode) domDocument.getDocumentElement(), npm);
			} else {
				translator.reset((IDOMNode) domDocument.getFirstChild(), npm);
			}

			translator.translate();

			final IJavaProject javaProject = JavaCore.create(jspFile.getProject());

			return new JSPTranslationPrime(javaProject, translator, jspFile);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (jspModel != null) {
				jspModel.releaseFromRead();
			}
		}

		return null;
	}


}
