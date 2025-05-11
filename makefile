serve:
	nix shell nixpkgs#leiningen --command lein ring server-headless
