const SavePostmanCollection = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/SavePostmanCollection/`, baseUrl);
	if (parameters.moduleCode !== undefined) {
		url.searchParams.append('moduleCode', parameters.moduleCode);
	}

	return fetch(url.toString(), {
		method: 'GET'
	});
}

const SavePostmanCollectionForm = (container) => {
	const html = `<form id='SavePostmanCollection-form'>
		<div id='SavePostmanCollection-moduleCode-form-field'>
			<label for='moduleCode'>moduleCode</label>
			<input type='text' id='SavePostmanCollection-moduleCode-param' name='moduleCode'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const moduleCode = container.querySelector('#SavePostmanCollection-moduleCode-param');

	container.querySelector('#SavePostmanCollection-form button').onclick = () => {
		const params = {
			moduleCode : moduleCode.value !== "" ? moduleCode.value : undefined
		};

		SavePostmanCollection(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { SavePostmanCollection, SavePostmanCollectionForm };